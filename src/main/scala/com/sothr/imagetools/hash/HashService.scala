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
    val grayImage = ImageService.convertToGray(ImageIO.read(new File(imagePath)))

    if (PropertiesService.get(PropertiesEnum.UseAhash.toString) == "true") {
      ahash = getAhash(grayImage, true)
    }
    if (PropertiesService.get(PropertiesEnum.UseAhash.toString) == "true") {
      dhash = getDhash(grayImage, true)
    }
    if (PropertiesService.get(PropertiesEnum.UseAhash.toString) == "true") {
      phash = getPhash(grayImage, true)
    }
    
    val hashes = new ImageHashDTO(ahash, dhash, phash)
    debug(s"Generated hashes: $hashes")
    
    return hashes
  }

  def getAhash(image:BufferedImage, alreadyGray:Boolean = false):Long = {
    debug("Started generating an AHash")
    var grayImage:BufferedImage = null
    if (alreadyGray) {
      grayImage = image
    } else {
      grayImage = ImageService.convertToGray(image)
    }
    val resizedImage = ImageService.resize(grayImage, PropertiesService.get(PropertiesEnum.AhashPrecision.toString).toInt, true)
    val imageData = ImageService.getImageData(resizedImage)
    AHash.getHash(imageData)
  }

  def getDhash(image:BufferedImage, alreadyGray:Boolean = false):Long = {
    debug("Started generating an DHash")
    var grayImage:BufferedImage = null
    if (alreadyGray) {
      grayImage = image
    } else {
      grayImage = ImageService.convertToGray(image)
    }
    val resizedImage = ImageService.resize(grayImage, PropertiesService.get(PropertiesEnum.DhashPrecision.toString).toInt, true)
    val imageData = ImageService.getImageData(resizedImage)
    DHash.getHash(imageData)
  }

  def getPhash(image:BufferedImage, alreadyGray:Boolean = false):Long = {
    debug("Started generating an PHash")
    var grayImage:BufferedImage = null
    if (alreadyGray) {
      grayImage = image
    } else {
      grayImage = ImageService.convertToGray(image)
    }
    val resizedImage = ImageService.resize(grayImage, PropertiesService.get(PropertiesEnum.PhashPrecision.toString).toInt, true)
    val imageData = ImageService.getImageData(resizedImage)
    PHash.getHash(imageData)
  }
  
  def areAhashSimilar(ahash1:Long, ahash2:Long):Boolean = {
    val tolerence = PropertiesService.get(PropertiesEnum.AhashTolerance.toString).toInt
    val distance = Hamming.getDistance(ahash1, ahash2)
    debug(s"hash1: $ahash1 hash2: $ahash2 tolerence: $tolerence hamming distance: $distance")
    if (distance <= tolerence) true else false
  }
  
  def areDhashSimilar(dhash1:Long, dhash2:Long):Boolean = {
    val tolerence = PropertiesService.get(PropertiesEnum.DhashTolerance.toString).toInt
    val distance = Hamming.getDistance(dhash1, dhash2)
    debug(s"hash1: $dhash1 hash2: $dhash2 tolerence: $tolerence hamming distance: $distance")
    if (distance <= tolerence) true else false
  }
  
  def arePhashSimilar(phash1:Long, phash2:Long):Boolean = {
    val tolerence = PropertiesService.get(PropertiesEnum.PhashTolerance.toString).toInt
    val distance = Hamming.getDistance(phash1, phash2)
    debug(s"hash1: $phash1 hash2: $phash2 tolerence: $tolerence hamming distance: $distance")
    if (distance <= tolerence) true else false
  }

  def areImageHashesSimilar(imageHash1:ImageHashDTO, imageHash2:ImageHashDTO):Boolean = {
    //ahash
    val aHashTolerance = PropertiesService.get(PropertiesEnum.AhashTolerance.toString).toInt
    val aHashWeight = PropertiesService.get(PropertiesEnum.AhashWeight.toString).toFloat
    val useAhash = PropertiesService.get(PropertiesEnum.UseAhash.toString).toBoolean
    //dhash
    val dHashTolerance = PropertiesService.get(PropertiesEnum.DhashTolerance.toString).toInt
    val dHashWeight = PropertiesService.get(PropertiesEnum.DhashWeight.toString).toFloat
    val useDhash = PropertiesService.get(PropertiesEnum.UseDhash.toString).toBoolean
    //phash
    val pHashTolerance = PropertiesService.get(PropertiesEnum.PhashTolerance.toString).toInt
    val pHashWeight = PropertiesService.get(PropertiesEnum.PhashWeight.toString).toFloat
    val usePhash = PropertiesService.get(PropertiesEnum.UsePhash.toString).toBoolean

    //calculate weighted values
    var weightedHammingTotal:Float = 0
    var weightedToleranceTotal:Float = 0
    var methodsTotal = 0

    if (useAhash)
    {
      val hamming = Hamming.getDistance(imageHash1.getAhash(), imageHash2.getAhash())
      weightedHammingTotal += hamming * aHashWeight
      weightedToleranceTotal += aHashTolerance * aHashWeight
      debug(s"hash1: ${imageHash1.getAhash()} hash2: ${imageHash1.getAhash()} tolerence: $aHashTolerance hamming distance: $hamming weight: $aHashWeight")
      methodsTotal+=1
    }
    if (useDhash)
    {
      val hamming = Hamming.getDistance(imageHash1.getDhash(), imageHash2.getDhash())
      weightedHammingTotal += hamming * dHashWeight
      weightedToleranceTotal += dHashTolerance * dHashWeight
      debug(s"hash1: ${imageHash1.getDhash()} hash2: ${imageHash1.getDhash()} tolerence: $dHashTolerance hamming distance: $hamming weight: $dHashWeight")
      methodsTotal+=1
    }
    if (usePhash)
    {
      val hamming = Hamming.getDistance(imageHash1.getPhash(), imageHash2.getPhash())
      weightedHammingTotal += hamming * pHashWeight
      weightedToleranceTotal += pHashTolerance * pHashWeight
      debug(s"hash1: ${imageHash1.getPhash()} hash2: ${imageHash1.getPhash()} tolerence: $pHashTolerance hamming distance: $hamming weight: $pHashWeight")
      methodsTotal+=1
    }
    val weightedHammingMean = weightedHammingTotal / methodsTotal
    val weightedToleranceMean = weightedToleranceTotal /methodsTotal
    debug(s"Weighted Values Are: Hamming: $weightedHammingMean Tolerance: $weightedToleranceMean")

    if (weightedHammingMean <= weightedToleranceMean) true else false
  }

}
