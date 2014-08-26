package com.sothr.imagetools.engine.hash

import grizzled.slf4j.Logging

/**
 * Created by dev on 1/22/14.
 */
object AHash extends PerceptualHasher with Logging {
  def getHash(imageData: Array[Array[Int]]): Long = {
    //debug("Generating AHash")
    val width = imageData.length
    val height = imageData(0).length
    //debug(s"Image data size: ${width}x${height}")

    //calculate average pixel
    var total = 0
    for (row <- 0 until height) {
      for (col <- 0 until width) {
        total += imageData(row)(col)
      }
    }
    val mean = total / (height * width)

    //calculate ahash
    var hash = 0L
    for (row <- 0 until height by 2) {
      //process each column
      for (col <- 0 until width by 1) {
        hash <<= 1
        val pixel = imageData(row)(col)
        //If the current pixel is at or above the mean, store it as a one, else store it as a zero
        if (pixel >= mean) hash |= 1 else hash |= 0
      }

      if ((row + 1) < width) {
        val nextRow = row + 1
        //process each column
        for (col <- (width - 1) to 0 by -1) {
          hash <<= 1
          val pixel = imageData(nextRow)(col)
          if (pixel >= mean) hash |= 1 else hash |= 0
        }
      }
    }
    //debug(s"Computed AHash: $hash from ${width * height} pixels")
    hash
  }
}
