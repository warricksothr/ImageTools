package com.sothr.imagetools

import java.io.File
import akka.actor.{Actor, Props, ActorLogging}
import akka.routing.{Broadcast, RoundRobinRouter}
import akka.event.Logging
import scala.collection.mutable.MutableList
import com.sothr.imagetools.image.Image

//exeternal cases
case class EngineProcessFile(file:File)
case object EngineNoMoreFiles
case object EngineIsProcessingFinished
case object EngineGetProcessingResults

//internal cases
case class EngineFileProcessed(image:Image)
case object EngineActorProcessingFinished
case object EngineActorReactivate

class ConcurrentEngine extends Actor with ActorLogging {
    val imageCache = AppConfig.cacheManager.getCache("images")
    val numOfRouters = 10
    val router = context.actorOf(Props[ConcurrentEngineActor].withRouter(RoundRobinRouter(nrOfInstances = numOfRouters)))
    
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
        if (command.image != null) {
            log.debug(s"processed image: ${command.image.imagePath}")
            images += command.image
        }
    }
    
    /*
     * 
     */
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
        case _      => log.info("received unknown message")
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