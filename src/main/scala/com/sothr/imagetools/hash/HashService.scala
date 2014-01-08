package com.sothr.imagetools.hash

import com.sothr.imagetools.dto.ImageHashDTO
import com.sothr.imagetools.image.Image
import com.sothr.imagetools.util.{PropertiesEnum, PropertiesService}

object HashService {

  def getImageHashes(image:Image):ImageHashDTO = {
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
    return new ImageHashDTO(ahash, dhash, phash)
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
