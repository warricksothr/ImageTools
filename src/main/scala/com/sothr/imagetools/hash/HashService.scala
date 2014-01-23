package com.sothr.imagetools.hash

import grizzled.slf4j.Logging
import com.sothr.imagetools.dto.ImageHashDTO
import com.sothr.imagetools.util.{PropertiesEnum, PropertiesService, Hamming}
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
    debug("Started generating an AHash")
    val grayImage = ImageService.convertToGray(image)
    val resizedImage = ImageService.resize(grayImage, PropertiesService.get(PropertiesEnum.AhashPrecision.toString).toInt, true)
    val imageData = ImageService.getImageData(resizedImage)
    AHash.getHash(imageData)
  }

  def getDhash(image:BufferedImage):Long = {
    debug("Started generating an DHash")
    val grayImage = ImageService.convertToGray(image)
    val resizedImage = ImageService.resize(grayImage, PropertiesService.get(PropertiesEnum.DhashPrecision.toString).toInt, true)
    val imageData = ImageService.getImageData(resizedImage)
    DHash.getHash(imageData)
  }

  def getPhash(image:BufferedImage):Long = {
    debug("Started generating an PHash")
    val grayImage = ImageService.convertToGray(image)
    val resizedImage = ImageService.resize(grayImage, PropertiesService.get(PropertiesEnum.PhashPrecision.toString).toInt, true)
    val imageData = ImageService.getImageData(resizedImage)
    PHash.getHash(imageData)
  }
  
  def areAhashSimilar(ahash1:Long, ahash2:Long):Boolean = {
      val tolerence = PropertiesService.get(PropertiesEnum.AhashTolerence.toString).toInt
      val distance = Hamming.getDistance(ahash1, ahash2)
      debug(s"hash1: $ahash1 hash2: $ahash2 tolerence: $tolerence hamming distance: $distance")
      if (distance <= tolerence) true else false
  }
  
  def areDhashSimilar(dhash1:Long, dhash2:Long):Boolean = {
      val tolerence = PropertiesService.get(PropertiesEnum.DhashTolerence.toString).toInt
      val distance = Hamming.getDistance(dhash1, dhash2)
      debug(s"hash1: $dhash1 hash2: $dhash2 tolerence: $tolerence hamming distance: $distance")
      if (distance <= tolerence) true else false
  }
  
  def arePhashSimilar(phash1:Long, phash2:Long):Boolean = {
      val tolerence = PropertiesService.get(PropertiesEnum.PhashTolerence.toString).toInt
      val distance = Hamming.getDistance(phash1, phash2)
      debug(s"hash1: $phash1 hash2: $phash2 tolerence: $tolerence hamming distance: $distance")
      if (distance <= tolerence) true else false
  }

}
