package com.sothr.imagetools.engine

import java.io.File

import akka.actor._
import com.sothr.imagetools.engine.image._
import com.sothr.imagetools.engine.util.DirectoryFilter
import grizzled.slf4j.Logging

import scala.collection.immutable.HashSet
import scala.collection.mutable
import scala.util.control.Breaks._

/**
 * Engine definition
 *
 * Created by drew on 1/26/14.
 */
abstract class Engine extends Logging {
  val system = ActorSystem("EngineActorSystem")
  val imageFilter: ImageFilter = new ImageFilter()
  val imageCache = AppConfig.cacheManager.getCache("images")

  //file search listener
  var searchedListener = system.actorOf(Props[DefaultLoggingEngineListener],
    name = "SearchedEngineListener")

  def setSearchedListener(listenerRef: ActorRef) = {
    this.searchedListener = listenerRef
  }

  def setProcessedListener(listenerType: ActorRef)

  def setSimilarityListener(listenerType: ActorRef)

  def getAllImageFiles(directoryPath: String, recursive: Boolean = false, recursiveDepth: Int = 500): List[File] = {
    val fileList = new mutable.MutableList[File]()
    if (directoryPath != null && directoryPath != "") {
      val directory: File = new File(directoryPath)
      val imageFilter = new ImageFilter
      if (directory.isDirectory) {
        val files = directory.listFiles(imageFilter)
        if (files != null) {
          fileList ++= files
          debug(s"Found ${files.length} files that are images in directory: $directoryPath")
          if (recursive) {
            val directoryFilter = new DirectoryFilter
            val directories = directory.listFiles(directoryFilter)
            for (directory <- directories) {
              fileList ++= getAllImageFiles(directory.getAbsolutePath, recursive, recursiveDepth - 1)
              this.searchedListener ! SubmitMessage(s"Found ${fileList.length} files to process")
            }
          } else {
            this.searchedListener ! SubmitMessage(s"Found ${fileList.length} files to process")
          }
        }
      }
    }
    fileList.toList
  }

  /**
   * Get all images for a directory with hashes
   */
  def getImagesForDirectory(directoryPath: String, recursive: Boolean = false, recursiveDepth: Int = 500): List[Image]

  /**
   * Get all similar images for a directory with hashes
   */
  def getSimilarImagesForDirectory(directoryPath: String, recursive: Boolean = false, recursiveDepth: Int = 500): List[SimilarImages]

  def deleteImage(image: Image): Unit = {
    ImageService.deleteImage(image)
  }

  def deleteImages(images: List[Image]): Unit = {
    for (image <- images) {
      deleteImage(image)
    }
  }

  /**
   * Go through the list of similarities and group them so that they represent actual similarities
   *
   * For example. A is similar to B and C is similar to B but A is was not considered similar to C. Therefore A B and C should be considered similar unless otherwise noted.
   *
   * @param similarList a list of detected similar images
   * @return a grouped and combined list of similar images
   */
  def processSimilarities(similarList: List[SimilarImages]): List[SimilarImages] = {
    //process the result into a list we want in cleanedSimilarImages
    /*
      Go through all the images. If a similar image for the image doesn't exist, create it. if it does,
      add it to that similar image. The end result is that all images should be grouped according to
      their similar images and images that are similar to them.
     */
    var count = 0
    // similar image mapping to map known images back to their similar set
    val similarImageMap = new mutable.HashMap[Image, SimilarImages]()

    // List of the actual similar image sets
    val cleanedSimilarImages = new mutable.MutableList[SimilarImages]()

    // loop over all of the similar image sets
    for (similarImages <- similarList) {
      count += 1
      if (count % 25 == 0 || count == similarList.length) {
        debug(s"Cleaning similar images. $count/${similarList.length} ${similarList.length - count} left to clean")
        this.searchedListener ! SubmitMessage(s"Cleaning similar images. $count/${similarList.length}")
      }
      var foundSimilarity = false
      var similarity:SimilarImages = null
      breakable { for (similarImage <- similarImages.similarImages) {
        if (similarImageMap.contains(similarImage)) {
          similarity = similarImageMap(similarImage)
          foundSimilarity = true
          break()
        }
      } }

      //if no similarity was found, one should be created
      if (!foundSimilarity) {
        similarity = new SimilarImages(new HashSet[Image])
        // the created similarity is added to the cleaned list
        cleanedSimilarImages += similarity
      }

      // all images should be added to this new similarity
      similarity.similarImages = similarity.similarImages ++ similarImages.similarImages
      similarImages.similarImages.foreach(img => similarImageMap.put(img, similarity))
    }

    //get a count of similar images found
    var totalCount = 0
    cleanedSimilarImages.foreach(img => totalCount += img.similarImages.size)
    debug(s"Found $totalCount images with similarities")
    this.searchedListener ! SubmitMessage(s"Found $totalCount images with similarities")

    // Sort the similarImages by ?!?!?!? and return
    cleanedSimilarImages.toList.sorted(SimilarImagesOrdering)
  }
}

case class SubmitMessage(message: String)

case class ScannedFileCount(count: Integer, total: Integer, message: String = null)

case class ComparedFileCount(count: Integer, total: Integer, message: String = null)

abstract class EngineListener extends Actor with ActorLogging {
  override def receive: Actor.Receive = {
    case command: SubmitMessage => handleMessage(command)
    case command: ScannedFileCount => handleScannedFileCount(command)
    case command: ComparedFileCount => handleComparedFileCount(command)
    case _ => log.info("received unknown message")
  }

  def handleMessage(command: SubmitMessage)

  def handleScannedFileCount(command: ScannedFileCount)

  def handleComparedFileCount(command: ComparedFileCount)
}

/**
 * Actor for logging output information
 */
class DefaultLoggingEngineListener extends EngineListener with ActorLogging {
  override def handleComparedFileCount(command: ComparedFileCount): Unit = {
    if (command.message != null) {
      log.info(command.message)
    }
    log.info("Processed {}/{}", command.count, command.total)
  }

  override def handleScannedFileCount(command: ScannedFileCount): Unit = {
    if (command.message != null) {
      log.info(command.message)
    }
    log.info("Scanned {}/{} For Similarities", command.count, command.total)
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