package com.sothr.imagetools

import grizzled.slf4j.Logging
import java.awt.image.{DataBufferByte, BufferedImage, ColorConvertOp}
import net.coobird.thumbnailator.Thumbnails
import java.io.File
import com.sothr.imagetools.image.Image
import com.sothr.imagetools.hash.HashService
import javax.imageio.ImageIO

object ImageService extends Logging {

  def getImage(file:File):Image = {
    val thumbnailPath = getThumbnailPath(file)
    val bufferedImage = ImageIO.read(file)
    val hashes = HashService.getImageHashes(bufferedImage, file.getAbsolutePath)
    val imageSize = { (bufferedImage.getWidth, bufferedImage.getHeight) }
    val image = new Image(file.getAbsolutePath, thumbnailPath, imageSize, hashes)
    debug(s"Created image: $image")
    image
  }

  def getThumbnailPath(file:File):String = {
    "."
  }

  /**
   * Get the raw data for an image
   */
  def getImageData(image:BufferedImage):Array[Array[Int]] = {
    return convertTo2DWithoutUsingGetRGB(image)
  }

  /**
   * Quickly convert an image to grayscale
   *
   * @param image
   * @return
   */
  def convertToGray(image:BufferedImage):BufferedImage = {
    debug("Converting an image to grayscale")
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

  def resize(image:BufferedImage, size:Int, forced:Boolean=false):BufferedImage = {
    debug(s"Resizing an image to size: ${size}x${size} forced: $forced")
    if (forced) {
      Thumbnails.of(image).forceSize(size,size).asBufferedImage
    } else {
      Thumbnails.of(image).size(size,size).asBufferedImage
    }
  }

  /**
   * Convert a buffered image into a 2d pixel data array
   *
   * @param image
   * @return
   */
  private def convertTo2DWithoutUsingGetRGB(image:BufferedImage):Array[Array[Int]] = {

    val pixels = image.getRaster.getDataBuffer.asInstanceOf[DataBufferByte].getData
    val numPixels = pixels.length
    val width = image.getWidth
    val height = image.getHeight
    val isSingleChannel = if(numPixels == (width * height)) true else false
    val hasAlphaChannel = image.getAlphaRaster != null
    debug(s"Converting image to 2d. width:$width height:$height")

    val result = Array.ofDim[Int](height,width)
    if (isSingleChannel) {
      debug(s"Processing Single Channel Image")
      val pixelLength = 1
      var row = 0
      var col = 0
      debug(s"Processing pixels 0 until $numPixels by $pixelLength")
      for (pixel <- 0 until numPixels by pixelLength) {
        //debug(s"Processing pixel: $pixel/${numPixels - 1}")
        val argb:Int = pixels(pixel).toInt //singleChannel
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
      debug(s"Processing Four Channel Image")
      val pixelLength = 4
      var row = 0
      var col = 0
      debug(s"Processing pixels 0 until $numPixels by $pixelLength")
      for (pixel <- 0 until numPixels by pixelLength) {
        //debug(s"Processing pixel: $pixel/${numPixels - 1}")
        var argb:Int = 0
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
      debug(s"Processing Three Channel Image")
      val pixelLength = 3
      var row = 0
      var col = 0
      debug(s"Processing pixels 0 until $numPixels by $pixelLength")
      for (pixel <- 0 until numPixels by pixelLength) {
        //debug(s"Processing pixel: $pixel/${numPixels - 1}")
        var argb:Int = 0
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
  
}
