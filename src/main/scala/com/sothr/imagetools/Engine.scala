package com.sothr.imagetools

import com.sothr.imagetools.image.{SimilarImages, ImageFilter, Image}
import scala.collection.immutable
import scala.collection.mutable
import java.io.File
import grizzled.slf4j.Logging

/**
 * Created by drew on 1/26/14.
 */
class Engine extends Logging{

  val imageFilter:ImageFilter = new ImageFilter()

  def getImagesForDirectory(directoryPath:String):List[Image] = {
    val images:mutable.MutableList[Image] = new mutable.MutableList[Image]()
    val directory:File = new File(directoryPath)
    if (directory.isDirectory) {
      val files = directory.listFiles(imageFilter)
      debug(s"Found ${files.length} files that are images in directory: $directoryPath")
      for (file <- files) {
        images += ImageService.getImage(file)
      }
    } else {
      error(s"Provided path: $directoryPath is not a directory")
    }
    images.toList
  }

  def getSimilarImagesForDirectory(directoryPath:String):List[SimilarImages] = {
    null
  }

}
