package com.sothr.imagetools

import com.sothr.imagetools.image.{ImageCache, SimilarImages, ImageFilter, Image}
import scala.collection.immutable
import scala.collection.mutable
import java.io.File
import grizzled.slf4j.Logging

/**
 * Created by drew on 1/26/14.
 */
class Engine(var imageCache:ImageCache = new ImageCache()) extends Logging{

  val imageFilter:ImageFilter = new ImageFilter()

  def getImagesForDirectory(directoryPath:String):List[Image] = {
    debug(s"Looking for images in directory: $directoryPath")
    val images:mutable.MutableList[Image] = new mutable.MutableList[Image]()
    val directory:File = new File(directoryPath)
    if (directory.isDirectory) {
      val files = directory.listFiles(imageFilter)
      debug(s"Found ${files.length} files that are images in directory: $directoryPath")
      for (file <- files) {
        if (imageCache.contains(file.getAbsolutePath)) {
          images += imageCache.get(file.getAbsolutePath)
        } else {
          images += ImageService.getImage(file)
        }
      }
    } else {
      error(s"Provided path: $directoryPath is not a directory")
    }
    images.toList
  }

  def getSimilarImagesForDirectory(directoryPath:String):List[SimilarImages] = {
    debug(s"Looking for similar images in directory: $directoryPath")
    val images = getImagesForDirectory(directoryPath)
    info(s"Searching ${images.length} images for similarities")
    val ignoreSet = new mutable.HashSet[Image]()
    val allSimilarImages = new mutable.MutableList[SimilarImages]()
    var processedCount = 0
    for (rootImage <- images) {
      if (!ignoreSet.contains(rootImage)) {
        info(s"Processed ${processedCount}/${images.length - ignoreSet.size} About ${images.length - processedCount} images to go")
        debug(s"Looking for images similar to: ${rootImage.imagePath}")
        ignoreSet += rootImage
        val similarImages = new mutable.MutableList[Image]()
        for (image <- images) {
          if (!ignoreSet.contains(image)) {
            if (rootImage.isSimilarTo(image)) {
              debug(s"Image: ${image.imagePath} is similar")
              similarImages += image
              ignoreSet += image
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
    allSimilarImages.toList
  }
}