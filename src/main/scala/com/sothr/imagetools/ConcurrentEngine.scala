package com.sothr.imagetools

import java.io.File
import akka.actor.{Actor, ActorSystem, Props, ActorLogging}
import akka.routing.{Broadcast, SmallestMailboxRouter}
import akka.event.Logging
import akka.pattern.ask
import akka.util.Timeout
import java.util.concurrent.TimeUnit
import scala.collection.mutable.{MutableList, HashSet}
import com.sothr.imagetools.image.{SimilarImages, ImageFilter, Image}
import com.sothr.imagetools.util.{PropertiesEnum, PropertiesService}
import scala.concurrent.{Await, blocking, Future}
import java.lang.Thread
import scala.concurrent.ExecutionContext.Implicits.global

// exeternal cases //
// processing files into images
case class EngineProcessFile(file:File)
case object EngineNoMoreFiles
case object EngineIsProcessingFinished
case object EngineGetProcessingResults
//finding similarities between images
case class EngineFindSimilarities(images:List[Image])
case object EngineIsSimilarityFinished
case object EngineGetSimilarityResults

//internal cases
case class EngineFileProcessed(image:Image)
case object EngineActorProcessingFinished
case class EngineCompareImages(image1:Image,image2:Image)
case object EngineActorCompareImagesRoundFinished
case object EngineActorCompareImagesFinished
case object EngineActorReactivate

class ConcurrentEngine extends Engine with grizzled.slf4j.Logging {
    val system = ActorSystem("EngineActorSystem")
    val engineController = system.actorOf(Props[ConcurrentEngineController], name = "EngineController")
    implicit val timeout = Timeout(5, TimeUnit.SECONDS)
    
  def getImagesForDirectory(directoryPath:String):List[Image] = {
    debug(s"Looking for images in directory: $directoryPath")
    val directory:File = new File(directoryPath)
    val images:MutableList[Image] = new MutableList[Image]()
    if (directory.isDirectory) {
      val files = directory.listFiles(imageFilter)
      info(s"Found ${files.length} files that are images in directory: $directoryPath")
      for (file <- files) {
        engineController ! EngineProcessFile(file)
      }
      engineController ! EngineNoMoreFiles
      var doneProcessing = false
      while(!doneProcessing) {
          val f = engineController ? EngineIsProcessingFinished
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
      val f = engineController ? EngineGetProcessingResults
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
    val ignoreSet = new HashSet[Image]()
    val allSimilarImages = new MutableList[SimilarImages]()
    var processedCount = 0
    var similarCount = 0
    for (rootImage <- images) {
      if (!ignoreSet.contains(rootImage)) {
        if (processedCount % 25 == 0) {
            info(s"Processed ${processedCount}/${images.length - ignoreSet.size} About ${images.length - processedCount} images to go")
        }
        debug(s"Looking for images similar to: ${rootImage.imagePath}")
        ignoreSet += rootImage
        val similarImages = new MutableList[Image]()
        for (image <- images) {
          if (!ignoreSet.contains(image)) {
            if (rootImage.isSimilarTo(image)) {
              debug(s"Image: ${image.imagePath} is similar")
              similarImages += image
              ignoreSet += image
              similarCount += 1
            }
          }
        }
        if (similarImages.length > 1) {
          val similar = new SimilarImages(rootImage, similarImages.toList)
          debug(s"Found similar images: ${similar.toString}")
          allSimilarImages += similar
        }
        processedCount += 1
      }
    }
    info(s"Finished processing ${images.size} images. Found $similarCount similar images")
    allSimilarImages.toList
  }
}

class ConcurrentEngineController extends Actor with ActorLogging {
    val imageCache = AppConfig.cacheManager.getCache("images")
    val numOfRouters = PropertiesService.get(PropertiesEnum.ConcurrentProcessingLimit.toString).toInt
    val router = context.actorOf(Props[ConcurrentEngineActor].withRouter(SmallestMailboxRouter(nrOfInstances = numOfRouters)))
    
    var images:MutableList[Image] = new MutableList[Image]()
    var toProcess = 0
    var processed = 0
    
    var processorsFinished = 0
    
    override def preStart() = {
      // initialization code
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
        if (processed % 25 == 0) log.info(s"Processed ${processed}/${toProcess}")
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

class ConcurrentEngineActor extends Actor with ActorLogging {
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