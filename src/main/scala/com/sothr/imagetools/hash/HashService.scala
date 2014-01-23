package com.sothr.imagetools.hash

import grizzled.slf4j.Logging
import com.sothr.imagetools.dto.ImageHashDTO
import com.sothr.imagetools.util.{PropertiesEnum, PropertiesService}
import com.sothr.imagetools.ImageService
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import java.io.File

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
    val image = ImageIO.read(new File(imagePath))

    if (PropertiesService.get(PropertiesEnum.UseAhash.toString) == "true") {
      ahash = getAhash(image)
    }
    if (PropertiesService.get(PropertiesEnum.UseAhash.toString) == "true") {
      dhash = getDhash(image)
    }
    if (PropertiesService.get(PropertiesEnum.UseAhash.toString) == "true") {
      phash = getPhash(image)
    }
    
    val hashes = new ImageHashDTO(ahash, dhash, phash)
    debug(s"Generated hashes: $hashes")
    
    return hashes
  }

  def getAhash(image:BufferedImage):Long = {
    debug("Generating an AHash")
    val grayImage = ImageService.convertToGray(image)
    val resizedImage = ImageService.resize(grayImage, PropertiesService.get(PropertiesEnum.AhashPrecision.toString).toInt, true)
    val imageData = ImageService.getImageData(resizedImage)
    AHash.getHash(imageData)
  }

  def getDhash(image:BufferedImage):Long = {
    debug("Generating an DHash")
    val grayImage = ImageService.convertToGray(image)
    val resizedImage = ImageService.resize(grayImage, PropertiesService.get(PropertiesEnum.DhashPrecision.toString).toInt, true)
    val imageData = ImageService.getImageData(resizedImage)
    DHash.getHash(imageData)
  }

  def getPhash(image:BufferedImage):Long = {
    debug("Generating an PHash")
    val grayImage = ImageService.convertToGray(image)
    val resizedImage = ImageService.resize(grayImage, PropertiesService.get(PropertiesEnum.PhashPrecision.toString).toInt, true)
    val imageData = ImageService.getImageData(resizedImage)
    PHash.getHash(imageData)
  }

}
