package com.sothr.imagetools.hash

import grizzled.slf4j.Logging
import com.sothr.imagetools.dto.ImageHashDTO
import com.sothr.imagetools.util.{PropertiesEnum, PropertiesService}

/**
 * A service that exposes the ability to construct perceptive hashes from an
 * image which can be used to find a perceptual difference between two or more
 * images
 */
object HashService extends Logging {

  def getImageHashes(imagePath:String):ImageHashDTO = {
    
    debug(s"Creating hashes for $imagePath")
    
    var ahash:Long = 0L
    var dhash:Long = 0L
    var phash:Long = 0L
    
    //Get Image Data
    var imageData:Array[Array[Int]] = null
    
    if (PropertiesService.get(PropertiesEnum.UseAhash.toString) == "true") {
      ahash = getAhash(imageData)
    }
    if (PropertiesService.get(PropertiesEnum.UseAhash.toString) == "true") {
      dhash = getDhash(imageData)
    }
    if (PropertiesService.get(PropertiesEnum.UseAhash.toString) == "true") {
      phash = getPhash(imageData)
    }
    
    val hashes = new ImageHashDTO(ahash, dhash, phash)
    debug(s"Generated hashes: $hashes")
    
    return hashes
  }

  def getAhash(imageData:Array[Array[Int]]):Long = {
    return 0L
  }

  def getDhash(imageData:Array[Array[Int]]):Long = {
    return 0L
  }

  def getPhash(imageData:Array[Array[Int]]):Long = {
    return 0L
  }

}
