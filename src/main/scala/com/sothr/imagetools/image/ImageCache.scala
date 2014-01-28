package com.sothr.imagetools.image

import scala.collection.mutable

/**
 * Created by drew on 1/26/14.
 */
class ImageCache {

  private val cache = new mutable.HashMap[String, Image]()

  def contains(imagePath:String) = cache.contains(imagePath)
  def get(imagePath:String) = cache(imagePath)
  def add(imagePath:String, image:Image) = cache.put(imagePath,image)
  def size:Int = cache.size

}
