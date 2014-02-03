package com.sothr.imagetools

import com.sothr.imagetools.image.{SimilarImages, ImageFilter, Image}
import com.sothr.imagetools.util.DirectoryFilter
import scala.collection.mutable
import java.io.File
import grizzled.slf4j.Logging

/**
 * Created by drew on 1/26/14.
 */
abstract class Engine extends Logging{

  val imageFilter:ImageFilter = new ImageFilter()
  val imageCache = AppConfig.cacheManager.getCache("images")

  def getAllImageFiles(directoryPath:String, recursive:Boolean=false, recursiveDepth:Int=500):List[File] = {
    val fileList = new mutable.MutableList[File]()
    val directory:File = new File(directoryPath)
    val imageFilter = new ImageFilter
    if (directory.isDirectory) {
      val files = directory.listFiles(imageFilter)
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
    fileList.toList
  }

  /**
   * Get all images for a directory with hashes
   */
  def getImagesForDirectory(directoryPath:String, recursive:Boolean=false, recursiveDepth:Int=500):List[Image];
  
  /**
   * Get all similar images for a directory with hashes
   */
  def getSimilarImagesForDirectory(directoryPath:String, recursive:Boolean=false, recursiveDepth:Int=500):List[SimilarImages];
}
