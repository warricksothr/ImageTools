package com.sothr.imagetools.hash

import grizzled.slf4j.Logging
import com.sothr.imagetools.dto.ImageHashDTO
import com.sothr.imagetools.util.{PropertiesEnum, PropertiesService, Hamming}
import com.sothr.imagetools.ImageService
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import java.io.{FileInputStream, File}
import org.apache.commons.codec.digest.DigestUtils

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
    val md5:String = getMD5(imagePath)

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
    
    val hashes = new ImageHashDTO(ahash, dhash, phash, md5)
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

  def getMD5(filePath:String):String = {
    DigestUtils.md5Hex(new FileInputStream(filePath))
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

  def getWeightedHashSimilarity(imageHash1:ImageHashDTO, imageHash2:ImageHashDTO):Float = {
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
    var methodsTotal = 0

    if (useAhash)
    {
      val hamming = Hamming.getDistance(imageHash1.ahash, imageHash2.ahash)
      weightedHammingTotal += hamming * aHashWeight
      debug(s"hash1: ${imageHash1.ahash} hash2: ${imageHash1.ahash} tolerence: $aHashTolerance hamming distance: $hamming weight: $aHashWeight")
      methodsTotal+=1
    }
    if (useDhash)
    {
      val hamming = Hamming.getDistance(imageHash1.dhash, imageHash2.dhash)
      weightedHammingTotal += hamming * dHashWeight
      debug(s"hash1: ${imageHash1.dhash} hash2: ${imageHash1.dhash} tolerence: $dHashTolerance hamming distance: $hamming weight: $dHashWeight")
      methodsTotal+=1
    }
    if (usePhash)
    {
      val hamming = Hamming.getDistance(imageHash1.phash, imageHash2.phash)
      weightedHammingTotal += hamming * pHashWeight
      debug(s"hash1: ${imageHash1.phash} hash2: ${imageHash1.phash} tolerence: $pHashTolerance hamming distance: $hamming weight: $pHashWeight")
      methodsTotal+=1
    }
    val weightedHammingMean = weightedHammingTotal / methodsTotal
    debug(s"Calculated Weighted Hamming Mean: $weightedHammingMean")
    weightedHammingMean
  }

  def getWeightedHashTolerence:Float = {
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
    var weightedToleranceTotal:Float = 0
    var methodsTotal = 0

    if (useAhash)
    {
      weightedToleranceTotal += aHashTolerance * aHashWeight
      debug(s"Ahash Tolerance: $aHashTolerance Current Weighted Tolerance: $weightedToleranceTotal")
      methodsTotal+=1
    }
    if (useDhash)
    {
      weightedToleranceTotal += dHashTolerance * dHashWeight
      debug(s"Dhash Tolerance: $dHashTolerance Current Weighted Tolerance: $weightedToleranceTotal")
      methodsTotal+=1
    }
    if (usePhash)
    {
      weightedToleranceTotal += pHashTolerance * pHashWeight
      debug(s"Phash Tolerance: $pHashTolerance Current Weighted Tolerance: $weightedToleranceTotal")
      methodsTotal+=1
    }
    val weightedTolerance = weightedToleranceTotal / methodsTotal
    debug(s"Calculated Weighted Tolerance: $weightedTolerance")
    weightedTolerance
  }

  def areImageHashesSimilar(imageHash1:ImageHashDTO, imageHash2:ImageHashDTO):Boolean = {
    val weightedHammingMean = getWeightedHashSimilarity(imageHash1, imageHash2)
    val weightedToleranceMean = getWeightedHashTolerence
    if (weightedHammingMean <= weightedToleranceMean) true else false
  }

}
