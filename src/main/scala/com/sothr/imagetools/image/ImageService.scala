package com.sothr.imagetools

import grizzled.slf4j.Logging
import java.awt.image.{DataBufferByte, BufferedImage}
import net.coobird.thumbnailator.Thumbnails

object ImageService extends Logging {
  
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
    val grayImage = new BufferedImage(image.getWidth, image.getHeight, BufferedImage.TYPE_BYTE_GRAY)
    val g = image.getGraphics
    g.drawImage(image,0,0,null)
    g.dispose()
    grayImage
  }

  def resize(image:BufferedImage, size:Int, forced:Boolean=false):BufferedImage = {
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
    val width = image.getWidth
    val height = image.getHeight
    val hasAlphaChannel = image.getAlphaRaster != null

    val result = Array.ofDim[Int](height,width)
    if (hasAlphaChannel) {
      val pixelLength = 4
      var row = 0
      var col = 0
      for (pixel <- 0 until pixels.length by pixelLength) {
        var argb:Int = 0
        argb += (pixels(pixel) & 0xff) << 24 //alpha
        argb += (pixels(pixel + 1) & 0xff) //blue
        argb += (pixels(pixel + 2) & 0xff) << 8 //green
        argb += (pixels(pixel + 3) & 0xff) << 16 //red
        result(row)(col) = argb
        col += 1
        if (col == width) {
          col = 0
          row += 1
        }
      }
    } else {
      val pixelLength = 3
      var row = 0
      var col = 0
      for (pixel <- 0 until pixels.length by pixelLength) {
        var argb:Int = 0
        argb += -16777216; // 255 alpha
        argb += (pixels(pixel) & 0xff) //blue
        argb += (pixels(pixel + 1) & 0xff) << 8 //green
        argb += (pixels(pixel + 2) & 0xff) << 16 //red
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