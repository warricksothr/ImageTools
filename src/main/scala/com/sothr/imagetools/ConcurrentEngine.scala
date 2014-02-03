package com.sothr.imagetools

import java.io.File
import akka.actor.{Actor, ActorSystem, Props, ActorLogging}
import akka.routing.{Broadcast, RoundRobinRouter, SmallestMailboxRouter}
import akka.pattern.ask
import akka.util.Timeout
import java.util.concurrent.TimeUnit
import com.sothr.imagetools.image.{SimilarImages, Image}
import com.sothr.imagetools.hash.HashService
import com.sothr.imagetools.util.{PropertiesEnum, PropertiesService}
import scala.concurrent.Await
import java.lang.Thread
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.mutable

class ConcurrentEngine extends Engine with grizzled.slf4j.Logging {
    val system = ActorSystem("EngineActorSystem")
    val engineProcessingController = system.actorOf(Props[ConcurrentEngineProcessingController], name = "EngineProcessingController")
    val engineSimilarityController = system.actorOf(Props[ConcurrentEngineSimilarityController], name = "EngineSimilarityController")
    implicit val timeout = Timeout(30, TimeUnit.SECONDS)
    
  def getImagesForDirectory(directoryPath:String):List[Image] = {
    debug(s"Looking for images in directory: $directoryPath")
    val directory:File = new File(directoryPath)
    val images:mutable.MutableList[Image] = new mutable.MutableList[Image]()
    if (directory.isDirectory) {
      val files = directory.listFiles(imageFilter)
      info(s"Found ${files.length} files that are images in directory: $directoryPath")
      for (file <- files) {
        engineProcessingController ! EngineProcessFile(file)
      }
      engineProcessingController ! EngineNoMoreFiles
      var doneProcessing = false
      while(!doneProcessing) {
          val f = engineProcessingController ? EngineIsProcessingFinished
          val result = Await.result(f, timeout.duration).asInstanceOf[Boolean]
          result match {
            case true =>
              doneProcessing = true
              debug("Processing Complete")
            case false => 
              debug("Still Processing")
              //sleep thread
              Thread.sleep(5000L)
              //val future = Future { blocking(Thread.sleep(5000L)); "done" }
          }
      }
      val f = engineProcessingController ? EngineGetProcessingResults
      val result = Await.result(f, timeout.duration).asInstanceOf[List[Image]]
      images ++= result
    } else {
      error(s"Provided path: $directoryPath is not a directory")
    }
    images.toList
  }

  //needs to be rebuilt
  def getSimilarImagesForDirectory(directoryPath:String):List[SimilarImages] = {
    debug(s"Looking for similar images in directory: $directoryPath")
    val images = getImagesForDirectory(directoryPath)
    engineSimilarityController ! EngineCompareSetImages(images)
    info(s"Searching ${images.length} images for similarities")
    for (rootImage <- images) {
      debug(s"Looking for images similar to: ${rootImage.imagePath}")
      engineSimilarityController ! EngineCompareImages(rootImage)
    }
    //tell the comparison engine there's nothing left to compare
    engineSimilarityController ! EngineNoMoreComparisons
    var doneProcessing = false
    while(!doneProcessing) {
      val f = engineSimilarityController ? EngineIsSimilarityFinished
      val result = Await.result(f, timeout.duration).asInstanceOf[Boolean]
      result match {
        case true =>
          doneProcessing = true
          debug("Processing Complete")
        case false =>
          debug("Still Processing")
          //sleep thread
          Thread.sleep(5000L)
        //val future = Future { blocking(Thread.sleep(5000L)); "done" }
      }
    }
    val f = engineSimilarityController ? EngineGetSimilarityResults
    val result = Await.result(f, timeout.duration).asInstanceOf[List[SimilarImages]]
    //process the result into a list we want in cleanedSimilarImages
    var count = 0
    val cleanedSimilarImages = new mutable.MutableList[SimilarImages]()
    val ignoreSet = new mutable.HashSet[Image]()
    for (similarImages <- result) {
        count += 1
        if (count % 25 == 0 || count == result.length) debug(s"Cleaning similar image $count/$result.length ${result.length-count} left to clean")
        if (!ignoreSet.contains(similarImages.rootImage)) {
            cleanedSimilarImages += similarImages
            ignoreSet += similarImages.rootImage
            for (image <- similarImages.similarImages) {
                ignoreSet += image
            }
        }
    }
    
    var similarCount = 0
    for (similarImage <- cleanedSimilarImages) {
      similarCount += 1 + similarImage.similarImages.size
    }

    info(s"Finished processing ${images.size} images. Found $similarCount similar images")
    cleanedSimilarImages.toList
  }
}


// exeternal cases //
// processing files into images
case class EngineProcessFile(file:File)
case object EngineNoMoreFiles
case object EngineIsProcessingFinished
case object EngineGetProcessingResults

//internal cases
case class EngineFileProcessed(image:Image)
case object EngineActorProcessingFinished
case object EngineActorReactivate

class ConcurrentEngineProcessingController extends Actor with ActorLogging {
    val imageCache = AppConfig.cacheManager.getCache("images")
    val numOfRouters = {
      val max = PropertiesService.get(PropertiesEnum.ConcurrentProcessingLimit.toString).toInt
      val processors = Runtime.getRuntime.availableProcessors()
      var threads = 0
      if (processors > max) threads = max else if (processors > 1) threads = processors - 1 else threads = 1
      threads
    }
    val router = context.actorOf(Props[ConcurrentEngineProcessingActor].withRouter(SmallestMailboxRouter(nrOfInstances = numOfRouters)))
    
    var images:mutable.MutableList[Image] = new mutable.MutableList[Image]()
    var toProcess = 0
    var processed = 0
    
    var processorsFinished = 0
    
    override def preStart() = {
        log.info("Staring the controller for processing images")
        log.info("Using {} actors to process images", numOfRouters)
    }
    
    override def receive = {
        case command:EngineProcessFile => processFile(command)
        case command:EngineFileProcessed => fileProcessed(command)
        case EngineNoMoreFiles => requestWrapup()
        case EngineActorProcessingFinished => actorProcessingFinished()
        case EngineIsProcessingFinished => isProcessingFinished()
        case EngineGetProcessingResults => getResults()
        case _ => log.info("received unknown message")
    }
    
    def processFile(command:EngineProcessFile) = {
        log.debug(s"Started evaluating ${command.file.getAbsolutePath}")
        toProcess += 1
        if (imageCache.isKeyInCache(command.file.getAbsolutePath)) {
            log.debug(s"${command.file.getAbsolutePath} was already processed")
            self ! EngineFileProcessed(imageCache.get(command.file.getAbsolutePath).getObjectValue.asInstanceOf[Image])
        } else {
            router ! command
        }
    }
    
    def fileProcessed(command:EngineFileProcessed) = {
        processed += 1
        if (processed % 25 == 0 || processed == toProcess) log.info(s"Processed $processed/$toProcess")
        if (command.image != null) {
            log.debug(s"processed image: ${command.image.imagePath}")
            images += command.image
        }
    }
    
    def requestWrapup() = {
        router ! Broadcast(EngineNoMoreFiles)
    }
    
    /*
     * Record that a processor is done
     */
    def actorProcessingFinished() = {
        processorsFinished += 1
    }
    
    /*
     * Check if processing is done 
     */
    def isProcessingFinished() = {
        try {
          if (processorsFinished >= numOfRouters) sender ! true else sender ! false
        } catch {
          case e: Exception ⇒
            sender ! akka.actor.Status.Failure(e)
            throw e
        }
    }
    
    /*
     * Get the results of the processing 
     */
    def getResults() = {
        try {
            processorsFinished = 0
            toProcess = 0
            processed = 0
            sender ! images.toList
            images.clear()
        } catch {
            case e: Exception ⇒
                sender ! akka.actor.Status.Failure(e)
                throw e
        }
    }
}

class ConcurrentEngineProcessingActor extends Actor with ActorLogging {
    var ignoreMessages = false
    override def receive = {
        case command:EngineProcessFile => processFile(command)
        case EngineNoMoreFiles => finishedProcessingFiles()
        case EngineActorReactivate => ignoreMessages = false
        case _ => log.info("received unknown message")
    }
    
    def processFile(command:EngineProcessFile) = {
        if (!ignoreMessages) {
            val image = ImageService.getImage(command.file)
            if (image != null) {
                sender ! EngineFileProcessed(image)
            } else {
                log.debug(s"Failed to process image: ${command.file.getAbsolutePath}")
            }
        }
    }
    
    def finishedProcessingFiles() = {
        if (!ignoreMessages) {
            ignoreMessages = true
            sender ! EngineActorProcessingFinished
        }
    }
}

//finding similarities between images
case class EngineCompareImages(image1:Image)
case class EngineCompareImagesComplete(similarImages:SimilarImages)
case class EngineCompareSetImages(images:List[Image])
case object EngineNoMoreComparisons
case object EngineIsSimilarityFinished
case object EngineGetSimilarityResults
case object EngineActorCompareImagesFinished

class ConcurrentEngineSimilarityController extends Actor with ActorLogging {
  val numOfRouters = {
    val max = PropertiesService.get(PropertiesEnum.ConcurrentSimiliartyLimit.toString).toInt
    val processors = Runtime.getRuntime.availableProcessors()
    var threads = 0
    if (processors > max) threads = max else if (processors > 1) threads = processors - 1 else threads = 1
    threads
  }
  val router = context.actorOf(Props[ConcurrentEngineSimilarityActor].withRouter(RoundRobinRouter(nrOfInstances = numOfRouters)))

  val allSimilarImages = new mutable.MutableList[SimilarImages]
  var numImages = 0
  var toProcess = 0
  var processed = 0

  var processorsFinished = 0

  override def preStart() = {
    log.info("Staring the controller for processing similarites between images")
    log.info("Using {} actors to process image similarites", numOfRouters)
  }

  override def receive = {
    case command:EngineCompareImages => findSimilarities(command)
    case command:EngineCompareImagesComplete => similarityProcessed(command)
    case command:EngineCompareSetImages => setImageList(command)
    case EngineNoMoreComparisons => requestWrapup()
    case EngineActorCompareImagesFinished => actorProcessingFinished()
    case EngineIsSimilarityFinished => isProcessingFinished()
    case EngineGetSimilarityResults => getResults()
    case _ => log.info("received unknown message")
  }

  def setImageList(command:EngineCompareSetImages) = {
      numImages = command.images.length
      router ! Broadcast(command)
  }

  def findSimilarities(command:EngineCompareImages) = {
    log.debug(s"Finding similarities between ${command.image1.imagePath} and $numImages images")
    toProcess += 1
    if (toProcess % 250 == 0 || toProcess == numImages) {
        log.info("Sent {}/{} images to be processed for similarites", toProcess, numImages)
    }
    //just relay the command to our workers
    router ! EngineCompareImages(command.image1)
  }

  def similarityProcessed(command:EngineCompareImagesComplete) = {
    processed += 1
    if (processed % 25 == 0 || processed == numImages) log.info(s"Processed $processed/$toProcess")
    if (command.similarImages != null) {
      log.debug(s"Found similar images: ${command.similarImages}")
      allSimilarImages += command.similarImages
    }
  }

  def requestWrapup() = {
    router ! Broadcast(EngineNoMoreComparisons)
  }

  /*
   * Record that a processor is done
   */
  def actorProcessingFinished() = {
    processorsFinished += 1
    log.debug("Similarity Processor Reported Finished")
  }

  /*
   * Check if processing is done
   */
  def isProcessingFinished() = {
    try {
      log.debug("Processors Finished {}/{}", processorsFinished, numOfRouters)
      if (processorsFinished >= numOfRouters) sender ! true else sender ! false
    } catch {
      case e: Exception ⇒
        sender ! akka.actor.Status.Failure(e)
        throw e
    }
  }

  /*
   * Get the results of the processing
   */
  def getResults() = {
    try {
      processorsFinished = 0
      toProcess = 0
      processed = 0
      sender ! allSimilarImages.toList
      allSimilarImages.clear()
    } catch {
      case e: Exception ⇒
        sender ! akka.actor.Status.Failure(e)
        throw e
    }
  }
}

class ConcurrentEngineSimilarityActor extends Actor with ActorLogging {
  var ignoreMessages = false
  var imageList = new mutable.MutableList[Image]()
  override def receive = {
    case command:EngineCompareImages => compareImages(command)
    case command:EngineCompareSetImages => cloneAndSetImages(command)
    case EngineNoMoreComparisons => finishedComparisons()
    case EngineActorReactivate => ignoreMessages = false
    case _ => log.info("received unknown message")
  }

  def cloneAndSetImages(command:EngineCompareSetImages) = {
      imageList.clear()
      for (image <- command.images) {
          imageList += image.cloneImage
      }
      log.debug("Added {} cloned images to internal list", imageList.length)
  }

  def compareImages(command:EngineCompareImages) = {
    if (!ignoreMessages) {
      val similarImages = new mutable.MutableList[Image]()
      for (image <- imageList) {
        if (!command.image1.equals(image)) {
          if (HashService.areImageHashesSimilar(command.image1.hashes, image.hashes)) {
            similarImages += image
          }
        }
      }
      //only send a message if we find similar images
      if (similarImages.length >= 1) {
        val similarImage = new SimilarImages(command.image1, similarImages.toList)
        log.debug(s"Found ${similarImage.similarImages.length} similar images to ${similarImage.rootImage}")
        sender ! EngineCompareImagesComplete(similarImage)
      } else {
        log.debug(s"Found no similar images to ${command.image1}")
        sender ! EngineCompareImagesComplete(null)
      }
    }
  }

  def finishedComparisons() = {
    if (!ignoreMessages) {
        log.info("Commanded to finish processing")
      ignoreMessages = true
      imageList.clear()
      log.debug("Finished processing comparisons")
      sender ! EngineActorCompareImagesFinished
    }
  }
}