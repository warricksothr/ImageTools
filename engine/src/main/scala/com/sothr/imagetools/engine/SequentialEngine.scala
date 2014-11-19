package com.sothr.imagetools.engine

import java.io.File

import akka.actor.{ActorRef, Props}
import com.sothr.imagetools.engine.image.{Image, ImageService, SimilarImages}
import grizzled.slf4j.Logging

import scala.collection.mutable

/**
 * Engine that works sequentially
 * Very Slow, but consistent. Excellent for testing
 *
 * Created by drew on 1/26/14.
 */
class SequentialEngine extends Engine with Logging {

  var processedListener = system.actorOf(Props[DefaultLoggingEngineListener],
    name = "ProcessedEngineListener")
  var similarityListener = system.actorOf(Props[DefaultLoggingEngineListener],
    name = "SimilarityEngineListener")

  override def setProcessedListener(listenerRef: ActorRef) = {
    this.processedListener = listenerRef
  }

  override def setSimilarityListener(listenerRef: ActorRef) = {
    this.similarityListener = listenerRef
  }

  def getSimilarImagesForDirectory(directoryPath: String, recursive: Boolean = false, recursiveDepth: Int = 500): List[SimilarImages] = {
    debug(s"Looking for similar images in directory: $directoryPath")
    val images = getImagesForDirectory(directoryPath, recursive, recursiveDepth)
    info(s"Searching ${images.length} images for similarities")
    val ignoreSet = new mutable.HashSet[Image]()
    val allSimilarImages = new mutable.MutableList[SimilarImages]()
    var processedCount = 0
    var similarCount = 0
    for (rootImage <- images) {
      if (!ignoreSet.contains(rootImage)) {
        if (processedCount % 25 == 0) {
          //info(s"Processed ${processedCount}/${images.length - ignoreSet.size} About ${images.length -
          //    processedCount} images to go")
          similarityListener ! ScannedFileCount(processedCount, images.length - ignoreSet.size)
        }
        debug(s"Looking for images similar to: ${rootImage.imagePath}")
        ignoreSet += rootImage
        val similarImages = new mutable.MutableList[Image]()
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
    this.processSimilarities(allSimilarImages.toList)
  }

  def getImagesForDirectory(directoryPath: String, recursive: Boolean = false, recursiveDepth: Int = 500): List[Image] = {
    debug(s"Looking for images in directory: $directoryPath")
    val images: mutable.MutableList[Image] = new mutable.MutableList[Image]()
    val imageFiles = getAllImageFiles(directoryPath, recursive, recursiveDepth)
    val directory: File = new File(directoryPath)
    var count = 0
    for (file <- imageFiles) {
      count += 1
      if (count % 25 == 0) {
        //info(s"Processed ${count}/${imageFiles.size}")
        processedListener ! ScannedFileCount(count, imageFiles.size)
      }
      val image = ImageService.getImage(file)
      if (image != null) {
        images += image
      }
    }
    images.toList
  }
}
