package com.sothr.imagetools.engine.image

import java.awt.image.{BufferedImage, ColorConvertOp, DataBufferByte}
import java.io.{File, IOException}
import javax.imageio.ImageIO

import com.sothr.imagetools.engine.AppConfig
import com.sothr.imagetools.engine.dao.ImageDAO
import com.sothr.imagetools.engine.hash.HashService
import com.sothr.imagetools.engine.util.{PropertiesService, PropertyEnum}
import grizzled.slf4j.Logging
import net.coobird.thumbnailator.Thumbnails
import net.sf.ehcache.Element
import org.hibernate.HibernateException

object ImageService extends Logging {

  val imageCache = AppConfig.cacheManager.getCache("images")
  private val imageDAO = new ImageDAO()

  def getImage(file: File): Image = {
    try {
      val image = lookupImage(file)
      if (image != null) {
        debug(s"${file.getAbsolutePath} was already processed")
        return image
      } else {
        debug(s"Processing image: ${file.getAbsolutePath}")
        val bufferedImage = ImageIO.read(file)
        val hashes = HashService.getImageHashes(bufferedImage, file.getAbsolutePath)
        var thumbnailPath = lookupThumbnailPath(hashes.getFileHash)
        if (thumbnailPath == null) thumbnailPath = getThumbnail(bufferedImage, hashes.getFileHash)
        val imageSize = {
          (bufferedImage.getWidth, bufferedImage.getHeight)
        }
        val image = new Image(file.getAbsolutePath, thumbnailPath, imageSize, hashes)
        debug(s"Created image: $image")
        return saveImage(image)
      }
    } catch {
      case ioe: IOException => error(s"Error processing ${file.getAbsolutePath}... ${ioe.getMessage}")
      case ex: Exception => error(s"Error processing ${file.getAbsolutePath}... ${ex.getMessage}", ex)
    }
    null
  }

  private def lookupImage(file: File): Image = {
    var image: Image = null
    var found = false
    //get from memory cache if possible
    try {
      if (imageCache.isKeyInCache(file.getAbsolutePath)) {
        image = imageCache.get(file.getAbsolutePath).getObjectValue.asInstanceOf[Image]
        found = true
      }
    } catch {
      case npe: NullPointerException => debug(s"\'${file.getAbsolutePath}\' was supposed to be in the cache, but was not")
    }
    //get from datastore if possible
    if (!found) {
      try {
        val tempImage = imageDAO.find(file.getAbsolutePath)
        if (tempImage != null) image = tempImage
      } catch {
        case ex: Exception => error(s"Error looking up \'${file.getAbsolutePath}\' was supposed to be in the database, but was not", ex)
      }
    }
    image
  }

  private def saveImage(image: Image): Image = {
    //save to cache
    imageCache.put(new Element(image.imagePath, image))
    //save to datastore
    try {
      imageDAO.save(image)
    } catch {
      case ex: Exception => error(s"Error saving \'${image.imagePath}\' to database", ex)
    }
    image
  }

  def lookupThumbnailPath(md5: String): String = {
    var thumbPath: String = null
    if (md5 != null) {
      //check for the actual file
      val checkPath = calculateThumbPath(md5)
      if (new File(checkPath).exists) thumbPath = checkPath
    } else {
      error("Null md5 passed in")
    }
    thumbPath
  }

  def calculateThumbPath(md5: String): String = {
    //break the path down into 4 char parts
    val subPath = md5.substring(0, 3)
    var path: String = s"${PropertiesService.get(PropertyEnum.ThumbnailDirectory.toString)}${PropertiesService.get(PropertyEnum.ThumbnailSize.toString)}/$subPath/"
    try {
      val dir = new File(path)
      if (!dir.exists()) dir.mkdirs()
    } catch {
      case ioe: IOException => error(s"Unable to create dirs for path: \'$path\'", ioe)
    }
    path += md5 + ".jpg"
    path
  }

  def getThumbnail(image: BufferedImage, md5: String): String = {
    //create thumbnail
    val thumb = resize(image, PropertiesService.get(PropertyEnum.ThumbnailSize.toString).toInt, forced = false)
    //calculate path
    val path = calculateThumbPath(md5)
    // save thumbnail to path
    try {
      ImageIO.write(thumb, "png", new File(path))
      debug(s"Wrote thumbnail to $path")
    } catch {
      case ioe: IOException => error(s"Unable to save thumbnail to $path", ioe)
    }
    // return path
    path
  }

  def resize(image: BufferedImage, size: Int, forced: Boolean = false): BufferedImage = {
    //debug(s"Resizing an image to size: ${size}x${size} forced: $forced")
    if (forced) {
      Thumbnails.of(image).forceSize(size, size).asBufferedImage
    } else {
      Thumbnails.of(image).size(size, size).asBufferedImage
    }
  }

  def deleteImage(image: Image) = {
    debug(s"Attempting to delete all traces of image: ${image.getImagePath}")
    try {
      val imageFile = new File(image.imagePath)
      //try to delete the file
      imageFile.delete()
      //purge the file from the database and cache
      this.imageCache.remove(imageFile.getAbsolutePath)
      this.imageDAO.delete(image)
    } catch {
      case se: SecurityException => error(s"Unable to delete file: ${image.getImagePath} due to a security exception", se)
      case ise: IllegalStateException => error(s"Unable to delete file: ${image.getImagePath} due to an illegal state exception", ise)
      case he: HibernateException => error(s"Unable to delete file: ${image.getImagePath} due to a hibernate exception", he)
    }
  }

  /**
   * Get the raw data for an image
   */
  def getImageData(image: BufferedImage): Array[Array[Int]] = {
    convertTo2DWithoutUsingGetRGB(image)
  }

  /**
   * Convert a buffered image into a 2d pixel data array
   *
   * @param image image to convert without using RGB
   * @return
   */
  private def convertTo2DWithoutUsingGetRGB(image: BufferedImage): Array[Array[Int]] = {

    val pixels = image.getRaster.getDataBuffer.asInstanceOf[DataBufferByte].getData
    val numPixels = pixels.length
    val width = image.getWidth
    val height = image.getHeight
    val isSingleChannel = if (numPixels == (width * height)) true else false
    val hasAlphaChannel = image.getAlphaRaster != null
    //debug(s"Converting image to 2d. width:$width height:$height")

    val result = Array.ofDim[Int](height, width)
    if (isSingleChannel) {
      //debug(s"Processing Single Channel Image")
      val pixelLength = 1
      var row = 0
      var col = 0
      //debug(s"Processing pixels 0 until $numPixels by $pixelLength")
      for (pixel <- 0 until numPixels by pixelLength) {
        //debug(s"Processing pixel: $pixel/${numPixels - 1}")
        val argb: Int = pixels(pixel).toInt //singleChannel
        //debug(s"Pixel data: $argb")
        result(row)(col) = argb
        col += 1
        if (col == width) {
          col = 0
          row += 1
        }
      }
    }
    else if (hasAlphaChannel) {
      //debug(s"Processing Four Channel Image")
      val pixelLength = 4
      var row = 0
      var col = 0
      //debug(s"Processing pixels 0 until $numPixels by $pixelLength")
      for (pixel <- 0 until numPixels by pixelLength) {
        //debug(s"Processing pixel: $pixel/${numPixels - 1}")
        var argb: Int = 0
        argb += pixels(pixel).toInt << 24 //alpha
        argb += pixels(pixel + 1).toInt //blue
        argb += pixels(pixel + 2).toInt << 8 //green
        argb += pixels(pixel + 3).toInt << 16 //red
        result(row)(col) = argb
        col += 1
        if (col == width) {
          col = 0
          row += 1
        }
      }
    } else {
      //debug(s"Processing Three Channel Image")
      val pixelLength = 3
      var row = 0
      var col = 0
      //debug(s"Processing pixels 0 until $numPixels by $pixelLength")
      for (pixel <- 0 until numPixels by pixelLength) {
        //debug(s"Processing pixel: $pixel/${numPixels - 1}")
        var argb: Int = 0
        argb += -16777216; // 255 alpha
        argb += pixels(pixel).toInt //blue
        argb += pixels(pixel + 1).toInt << 8 //green
        argb += pixels(pixel + 2).toInt << 16 //red
        result(row)(col) = argb
        col += 1
        if (col == width) {
          col = 0
          row += 1
        }
      }
    }
    result
  }

  /**
   * Quickly convert an image to grayscale
   *
   * @param image image to convert to greyscale
   * @return
   */
  def convertToGray(image: BufferedImage): BufferedImage = {
    //debug("Converting an image to grayscale")
    val grayImage = new BufferedImage(image.getWidth, image.getHeight, BufferedImage.TYPE_BYTE_GRAY)

    //create a color conversion operation
    val op = new ColorConvertOp(
      image.getColorModel.getColorSpace,
      grayImage.getColorModel.getColorSpace, null)

    //convert the image to grey
    val result = op.filter(image, grayImage)

    //val g = image.getGraphics
    //g.drawImage(image,0,0,null)
    //g.dispose()
    result
  }
}
