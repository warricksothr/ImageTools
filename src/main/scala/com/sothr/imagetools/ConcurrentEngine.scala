package com.sothr.imagetools

import java.io.File
import akka.actor.{Actor, ActorSystem, Props, ActorLogging}
import akka.routing.{Broadcast, SmallestMailboxRouter}
import akka.pattern.ask
import akka.util.Timeout
import java.util.concurrent.TimeUnit
import com.sothr.imagetools.image.{SimilarImages, Image}
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

  //needs to be rebuilt as a concurrent capable method
  def getSimilarImagesForDirectory(directoryPath:String):List[SimilarImages] = {
    debug(s"Looking for similar images in directory: $directoryPath")
    val images = getImagesForDirectory(directoryPath)
    info(s"Searching ${images.length} images for similarities")
    val allSimilarImages = new mutable.MutableList[SimilarImages]()
    for (rootImage <- images) {
      debug(s"Looking for images similar to: ${rootImage.imagePath}")
      engineSimilarityController ! EngineCompareImages(rootImage, images, null)
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
    allSimilarImages ++= result

    var similarCount = 0
    for (similarImage <- allSimilarImages) {
      similarCount += 1 + similarImage.similarImages.size
    }

    info(s"Finished processing ${images.size} images. Found $similarCount similar images")
    allSimilarImages.toList
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
        if (processed % 25 == 0) log.info(s"Processed $processed/$toProcess")
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
case class EngineCompareImages(image1:Image,images:List[Image],ignoreList:Set[Image])
case class EngineCompareImagesComplete(similarImages:SimilarImages)
case object EngineNoMoreComparisons
case object EngineIsSimilarityFinished
case object EngineGetSimilarityResults
case object EngineActorCompareImagesFinished

class ConcurrentEngineSimilarityController extends Actor with ActorLogging {
  val imageCache = AppConfig.cacheManager.getCache("images")
  val numOfRouters = {
    val max = PropertiesService.get(PropertiesEnum.ConcurrentSimiliartyLimit.toString).toInt
    val processors = Runtime.getRuntime.availableProcessors()
    var threads = 0
    if (processors > max) threads = max else if (processors > 1) threads = processors - 1 else threads = 1
    threads
  }
  val router = context.actorOf(Props[ConcurrentEngineSimilarityActor].withRouter(SmallestMailboxRouter(nrOfInstances = numOfRouters)))

  val allSimilarImages = new mutable.MutableList[SimilarImages]
  val ignoreList = new mutable.HashSet[Image]()
  var toProcess = 0
  var processed = 0

  var processorsFinished = 0

  override def receive = {
    case command:EngineCompareImages => findSimilarities(command)
    case command:EngineCompareImagesComplete => similarityProcessed(command)
    case EngineNoMoreComparisons => requestWrapup()
    case EngineActorCompareImagesFinished => actorProcessingFinished()
    case EngineIsSimilarityFinished => isProcessingFinished()
    case EngineGetSimilarityResults => getResults()
    case _ => log.info("received unknown message")
  }

  def findSimilarities(command:EngineCompareImages) = {
    log.debug(s"Finding similarities between ${command.image1.imagePath} and ${command.images.length} images")
    toProcess += 1
    //just relay the command to our workers
    router ! EngineCompareImages(command.image1, command.images, ignoreList.toSet[Image])
  }

  def similarityProcessed(command:EngineCompareImagesComplete) = {
    processed += 1
    if (processed % 25 == 0) log.info(s"Processed $processed/$toProcess")
    if (command.similarImages != null) {
      if (!ignoreList.contains(command.similarImages.rootImage)) {
        log.debug(s"Found similar images: ${command.similarImages}")
        allSimilarImages += command.similarImages
        //add the similar images to the ignore list so we don't re-process them constantly
        ignoreList += command.similarImages.rootImage
        ignoreList ++= command.similarImages.similarImages
      }
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
  override def receive = {
    case command:EngineCompareImages => compareImages(command)
    case EngineNoMoreComparisons => finishedComparisons()
    case EngineActorReactivate => ignoreMessages = false
    case _ => log.info("received unknown message")
  }

  def compareImages(command:EngineCompareImages) = {
    if (!ignoreMessages) {
      val similarImages = new mutable.MutableList[Image]()
      for (image <- command.images) {
        if (!command.ignoreList.contains(image) && command.image1 != image) {
          if (command.image1.isSimilarTo(image)) {
            similarImages += image
  var ignoreMessages = false
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
      ignoreMessages = true
      sender ! EngineActorCompareImagesFinished
    }
  }
}