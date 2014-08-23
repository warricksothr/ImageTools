package com.sothr.imagetools.hash

import java.awt.image.BufferedImage
import java.io.{File, FileInputStream}
import javax.imageio.ImageIO

import com.sothr.imagetools.dto.ImageHashDTO
import com.sothr.imagetools.image.ImageService
import com.sothr.imagetools.util.{Hamming, PropertiesService}
import grizzled.slf4j.Logging
import org.apache.commons.codec.digest.DigestUtils
import resource._

/**
 * A service that exposes the ability to construct perceptive hashes from an
 * image which can be used to find a perceptual difference between two or more
 * images
 */
object HashService extends Logging {

  def getImageHashes(imagePath:String):ImageHashDTO = {
    //debug(s"Creating hashes for $imagePath")
    getImageHashes(ImageIO.read(new File(imagePath)), imagePath)
  }

  def getImageHashes(image:BufferedImage, imagePath:String):ImageHashDTO = {
    //debug("Creating hashes for an image")

    var ahash:Long = 0L
    var dhash:Long = 0L
    var phash:Long = 0L
    val md5:String = getMD5(imagePath)

    //Get Image Data
    val grayImage = ImageService.convertToGray(image)

    if (PropertiesService.useAhash) {
      ahash = getAhash(grayImage, alreadyGray = true)
    }
    if (PropertiesService.useDhash) {
      dhash = getDhash(grayImage, alreadyGray = true)
    }
    if (PropertiesService.usePhash) {
      phash = getPhash(grayImage, alreadyGray = true)
    }

    val hashes = new ImageHashDTO(ahash, dhash, phash, md5)
    debug(s"Generated hashes: $hashes")

    hashes
  }

  def getAhash(image:BufferedImage, alreadyGray:Boolean = false):Long = {
    //debug("Started generating an AHash")
    var grayImage:BufferedImage = null
    if (alreadyGray) {
      grayImage = image
    } else {
      grayImage = ImageService.convertToGray(image)
    }
    val resizedImage = ImageService.resize(grayImage, PropertiesService.aHashPrecision, forced = true)
    val imageData = ImageService.getImageData(resizedImage)
    AHash.getHash(imageData)
  }

  def getDhash(image:BufferedImage, alreadyGray:Boolean = false):Long = {
    //debug("Started generating an DHash")
    var grayImage:BufferedImage = null
    if (alreadyGray) {
      grayImage = image
    } else {
      grayImage = ImageService.convertToGray(image)
    }
    val resizedImage = ImageService.resize(grayImage, PropertiesService.dHashPrecision, forced = true)
    val imageData = ImageService.getImageData(resizedImage)
    DHash.getHash(imageData)
  }

  def getPhash(image:BufferedImage, alreadyGray:Boolean = false):Long = {
    //debug("Started generating an PHash")
    var grayImage:BufferedImage = null
    if (alreadyGray) {
      grayImage = image
    } else {
      grayImage = ImageService.convertToGray(image)
    }
    val resizedImage = ImageService.resize(grayImage, PropertiesService.pHashPrecision, forced = true)
    val imageData = ImageService.getImageData(resizedImage)
    PHash.getHash(imageData)
  }

  def getMD5(filePath:String):String = {
    managed(new FileInputStream(filePath)) acquireAndGet {
      input =>
      DigestUtils.md5Hex(input)
    }
  }
  
  def areAhashSimilar(ahash1:Long, ahash2:Long):Boolean = {
    val tolerence = PropertiesService.aHashTolerance
    val distance = Hamming.getDistance(ahash1, ahash2)
    //debug(s"hash1: $ahash1 hash2: $ahash2 tolerence: $tolerence hamming distance: $distance")
    if (distance <= tolerence) true else false
  }
  
  def areDhashSimilar(dhash1:Long, dhash2:Long):Boolean = {
    val tolerence = PropertiesService.dHashTolerance
    val distance = Hamming.getDistance(dhash1, dhash2)
    //debug(s"hash1: $dhash1 hash2: $dhash2 tolerence: $tolerence hamming distance: $distance")
    if (distance <= tolerence) true else false
  }
  
  def arePhashSimilar(phash1:Long, phash2:Long):Boolean = {
    val tolerence = PropertiesService.pHashTolerance
    val distance = Hamming.getDistance(phash1, phash2)
    //debug(s"hash1: $phash1 hash2: $phash2 tolerence: $tolerence hamming distance: $distance")
    if (distance <= tolerence) true else false
  }

  def getWeightedHashSimilarity(imageHash1:ImageHashDTO, imageHash2:ImageHashDTO):Float = {
    //ahash
    val aHashTolerance = PropertiesService.aHashTolerance
    val aHashWeight = PropertiesService.aHashWeight
    val useAhash = PropertiesService.useAhash
    //dhash
    val dHashTolerance = PropertiesService.dHashTolerance
    val dHashWeight = PropertiesService.dHashWeight
    val useDhash = PropertiesService.useAhash
    //phash
    val pHashTolerance = PropertiesService.pHashTolerance
    val pHashWeight = PropertiesService.pHashWeight
    val usePhash = PropertiesService.useAhash

    //calculate weighted values
    var weightedHammingTotal:Float = 0
    var methodsTotal = 0

    if (useAhash)
    {
      val hamming = Hamming.getDistance(imageHash1.ahash, imageHash2.ahash)
      weightedHammingTotal += hamming * aHashWeight
      //debug(s"hash1: ${imageHash1.ahash} hash2: ${imageHash1.ahash} tolerence: $aHashTolerance hamming distance: $hamming weight: $aHashWeight")
      methodsTotal+=1
    }
    if (useDhash)
    {
      val hamming = Hamming.getDistance(imageHash1.dhash, imageHash2.dhash)
      weightedHammingTotal += hamming * dHashWeight
      //debug(s"hash1: ${imageHash1.dhash} hash2: ${imageHash1.dhash} tolerence: $dHashTolerance hamming distance: $hamming weight: $dHashWeight")
      methodsTotal+=1
    }
    if (usePhash)
    {
      val hamming = Hamming.getDistance(imageHash1.phash, imageHash2.phash)
      weightedHammingTotal += hamming * pHashWeight
      //debug(s"hash1: ${imageHash1.phash} hash2: ${imageHash1.phash} tolerence: $pHashTolerance hamming distance: $hamming weight: $pHashWeight")
      methodsTotal+=1
    }
    val weightedHammingMean = weightedHammingTotal / methodsTotal
    //debug(s"Calculated Weighted Hamming Mean: $weightedHammingMean")
    weightedHammingMean
  }

  def getWeightedHashTolerence:Float = {
    //ahash
    val aHashTolerance = PropertiesService.aHashTolerance
    val aHashWeight = PropertiesService.aHashWeight
    val useAhash = PropertiesService.useAhash
    //dhash
    val dHashTolerance = PropertiesService.dHashTolerance
    val dHashWeight = PropertiesService.dHashWeight
    val useDhash = PropertiesService.useAhash
    //phash
    val pHashTolerance = PropertiesService.pHashTolerance
    val pHashWeight = PropertiesService.pHashWeight
    val usePhash = PropertiesService.useAhash

    //calculate weighted values
    var weightedToleranceTotal:Float = 0
    var methodsTotal = 0

    if (useAhash)
    {
      weightedToleranceTotal += aHashTolerance * aHashWeight
      //debug(s"Ahash Tolerance: $aHashTolerance Current Weighted Tolerance: $weightedToleranceTotal")
      methodsTotal+=1
    }
    if (useDhash)
    {
      weightedToleranceTotal += dHashTolerance * dHashWeight
      //debug(s"Dhash Tolerance: $dHashTolerance Current Weighted Tolerance: $weightedToleranceTotal")
      methodsTotal+=1
    }
    if (usePhash)
    {
      weightedToleranceTotal += pHashTolerance * pHashWeight
      //debug(s"Phash Tolerance: $pHashTolerance Current Weighted Tolerance: $weightedToleranceTotal")
      methodsTotal+=1
    }
    val weightedTolerance = weightedToleranceTotal / methodsTotal
    //debug(s"Calculated Weighted Tolerance: $weightedTolerance")
    weightedTolerance
  }

  def areImageHashesSimilar(imageHash1:ImageHashDTO, imageHash2:ImageHashDTO):Boolean = {
    val weightedHammingMean = getWeightedHashSimilarity(imageHash1, imageHash2)
    val weightedToleranceMean = getWeightedHashTolerence
    if (weightedHammingMean <= weightedToleranceMean) true else false
  }

}
