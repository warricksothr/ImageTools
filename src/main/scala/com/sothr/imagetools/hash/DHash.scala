package com.sothr.imagetools.hash

import grizzled.slf4j.Logging

/**
 * Created by dev on 1/22/14.
 */
object DHash extends PerceptualHasher with Logging {
  def getHash(imageData: Array[Array[Int]]): Long = {
    debug("Generating DHash")
    val width = imageData.length
    val height = imageData(0).length
    debug(s"Image data size: ${width}x${height}")
    var hash = 0L
    for (row <- 0 until width) {
      //println(f"Row: $row%d")
      var previousPixel = imageData(row)(0)
      var previousLocation = (row, 0)
      
      //process each column
      for (col <- 0 until height) {
        debug(s"previousPixel: $previousPixel previousLocation: $previousLocation")
        //println(f"Column: $col%d")
        hash <<= 1
        val pixel = imageData(row)(col)
        //binary or the current bit based on whether the value
        //of the current pixel is greater or equal to the previous pixel
        if (pixel >= previousPixel) hash |= 1 else hash |= 0
        debug(s"hash: hash")
        previousPixel = pixel
        previousLocation = (row, col)
      }
    }
    hash
  }
}
