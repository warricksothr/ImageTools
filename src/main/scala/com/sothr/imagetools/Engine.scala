package com.sothr.imagetools

import com.sothr.imagetools.image.{SimilarImages, ImageFilter, Image}
import scala.collection.mutable
import java.io.File
import grizzled.slf4j.Logging
import net.sf.ehcache.Element

/**
 * Created by drew on 1/26/14.
 */
abstract class Engine extends Logging{

  val imageFilter:ImageFilter = new ImageFilter()
  val imageCache = AppConfig.cacheManager.getCache("images")

  /**
   * Get all images for a directory with hashes
   */
  def getImagesForDirectory(directoryPath:String):List[Image];
  
  /**
   * Get all similar images for a directory with hashes
   */
  def getSimilarImagesForDirectory(directoryPath:String):List[SimilarImages];
}
