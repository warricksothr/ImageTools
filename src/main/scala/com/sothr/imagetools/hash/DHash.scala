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
    
    //calculate dhash
    var hash = 0L
    var previousPixel = imageData(height-1)(width-1)
    var previousLocation = (height-1, width-1)
    if (height % 2 == 0) {
      previousPixel = imageData(height-1)(0)
      previousLocation = (height-1, 0)   
    }
    
    for (row <- 0 until height by 2) {
      //process each column
      for (col <- 0 until width by 1) {
        hash <<= 1
        val pixel = imageData(row)(col)
        //debug(s"previousPixel: $previousPixel currentPixel: $pixel previousLocation: $previousLocation currentLocation: (${row},${col})")
        //binary of the current bit based on whether the value
        //of the current pixel is greater or equal to the previous pixel
        if (pixel >= previousPixel) hash |= 1 else hash |= 0
        //debug(s"(${row},${col})=$pixel hash=${hash.toBinaryString}")
        previousPixel = pixel
        previousLocation = (row, col)
      }
      
      if ((row +1) < width) {
          val nextRow = row + 1
          //process each column
          for (col <- (width - 1) to 0 by -1) {
            hash <<= 1
            val pixel = imageData(nextRow)(col)
            //debug(s"previousPixel: $previousPixel currentPixel: $pixel previousLocation: $previousLocation currentLocation: (${nextRow},${col})")
            if (pixel >= previousPixel) hash |= 1 else hash |= 0
            //debug(s"(${row},${col})=$pixel hash=${hash.toBinaryString}")
            previousPixel = pixel
            previousLocation = (nextRow, col)
          }
      }
    }
    debug(s"Computed DHash: $hash from ${width * height} pixels")
    hash
  }
}
