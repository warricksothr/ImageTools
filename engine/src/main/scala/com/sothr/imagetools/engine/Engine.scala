package com.sothr.imagetools.engine

import java.io.File

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem}
import AppConfig
import com.sothr.imagetools.engine.image.{SimilarImages, ImageFilter, Image}
import com.sothr.imagetools.image.SimilarImages
import com.sothr.imagetools.engine.util.DirectoryFilter
import grizzled.slf4j.Logging

import scala.collection.mutable

/**
 * Engine definition
 *
 * Created by drew on 1/26/14.
 */
abstract class Engine extends Logging {
  val system = ActorSystem("EngineActorSystem")
  val imageFilter:ImageFilter = new ImageFilter()
  val imageCache = AppConfig.cacheManager.getCache("images")

  def setProcessedListener(listenerType: ActorRef)
  def setSimilarityListener(listenerType: ActorRef)

  def getAllImageFiles(directoryPath:String, recursive:Boolean=false, recursiveDepth:Int=500):List[File] = {
    val fileList = new mutable.MutableList[File]()
    if (directoryPath != null && directoryPath != "") {
      val directory:File = new File(directoryPath)
      val imageFilter = new ImageFilter
      if (directory.isDirectory) {
        val files = directory.listFiles(imageFilter)
        if (files != null) {
          fileList ++= files
          info(s"Found ${files.length} files that are images in directory: $directoryPath")
          if (recursive) {
            val directoryFilter = new DirectoryFilter
            val directories = directory.listFiles(directoryFilter)
            for (directory <- directories) {
                fileList ++= getAllImageFiles(directory.getAbsolutePath, recursive, recursiveDepth-1)
            }
          }
        }
      }
    }
    fileList.toList
  }

  /**
   * Get all images for a directory with hashes
   */
  def getImagesForDirectory(directoryPath:String, recursive:Boolean=false, recursiveDepth:Int=500):List[Image]
  
  /**
   * Get all similar images for a directory with hashes
   */
  def getSimilarImagesForDirectory(directoryPath:String, recursive:Boolean=false, recursiveDepth:Int=500):List[SimilarImages]
}

case class SubmitMessage(message:String)
case class ScannedFileCount(count:Integer, total:Integer, message:String=null)
case class ComparedFileCount(count:Integer,total:Integer, message:String=null)
abstract class EngineListener extends Actor with ActorLogging {
  override def receive: Actor.Receive = {
    case command:SubmitMessage => handleMessage(command)
    case command:ScannedFileCount => handleScannedFileCount(command)
    case command:ComparedFileCount => handleComparedFileCount(command)
    case _ => log.info("received unknown message")
  }

  def handleMessage(command:SubmitMessage)
  def handleScannedFileCount(command:ScannedFileCount)
  def handleComparedFileCount(command:ComparedFileCount)
}

/**
 * Actor for logging output information
 */
class DefaultLoggingEngineListener extends EngineListener with ActorLogging {
  override def handleComparedFileCount(command: ComparedFileCount): Unit = {
    if (command.message != null) {
      log.info(command.message)
    }
    log.info("Processed {}/{}",command.count,command.total)
  }

  override def handleScannedFileCount(command: ScannedFileCount): Unit = {
    if (command.message != null) {
      log.info(command.message)
    }
    log.info("Scanned {}/{} For Similarities",command.count,command.total)
  }

  override def handleMessage(command: SubmitMessage): Unit = {
    log.info(command.message)
  }
}

/**
 * Actor for writing progress out to the commandline
 */
class CLIEngineListener extends EngineListener with ActorLogging {
  override def handleComparedFileCount(command: ComparedFileCount): Unit = {
    if (command.message != null) {
      System.out.println(command.message)
    }
    System.out.println(s"Processed ${command.count}/${command.total}")
  }

  override def handleScannedFileCount(command: ScannedFileCount): Unit = {
    if (command.message != null) {
      System.out.println(command.message)
    }
    System.out.println(s"Scanned ${command.count}/${command.total} For Similarities")
  }

  override def handleMessage(command: SubmitMessage): Unit = {
    System.out.println(command.message)
  }
}