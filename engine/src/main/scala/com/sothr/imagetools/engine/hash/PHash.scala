package com.sothr.imagetools.engine.hash

import edu.emory.mathcs.jtransforms.dct.FloatDCT_2D
import grizzled.slf4j.Logging

/**
 * Complex perceptual hash
 * Uses FFT to get
 *
 * Created by dev on 1/22/14.
 */
object PHash extends PerceptualHasher with Logging {
  def getHash(imageData: Array[Array[Int]]): Long = {
    //convert the imageData into a FloatArray
    val width = imageData.length
    val height = imageData(0).length
    //debug(s"Starting with image of ${height}x${width} for PHash")

    val imageDataFloat: Array[Array[Float]] = Array.ofDim[Float](height, width)
    for (row <- 0 until height) {
      for (col <- 0 until width) {
        imageDataFloat(row)(col) = imageData(row)(col).toFloat
      }
    }
    //debug("Copied image data to float array for transform")
    //debug(s"\n${imageDataFloat.deep.mkString("\n")}")

    //perform transform on the data
    val dct: FloatDCT_2D = new FloatDCT_2D(height, width)
    dct.forward(imageDataFloat, true)
    //debug("Converted image data into DCT")
    //debug(s"\n${imageDataFloat.deep.mkString("\n")}")

    //extract the DCT data
    val dctDataWidth: Int = width / 4
    val dctDataHeight: Int = height / 4

    //calculate the mean
    var total = 0.0f
    for (row <- 0 until dctDataHeight) {
      for (col <- 0 until dctDataWidth) {
        total += imageDataFloat(row)(col)
      }
    }
    val mean = total / (dctDataHeight * dctDataWidth)
    //debug(s"Calculated mean as $mean from ${total}/${dctDataHeight * dctDataWidth}")

    //calculate the hash
    var hash = 0L
    for (row <- 0 until dctDataHeight by 2) {
      //process each column
      for (col <- 0 until dctDataWidth by 1) {
        hash <<= 1
        val pixel = imageDataFloat(row)(col)
        //If the current pixel is at or above the mean, store it as a one, else store it as a zero
        if (pixel >= mean) hash |= 1 else hash |= 0
      }

      if ((row + 1) < dctDataWidth) {
        val nextRow = row + 1
        //process each column
        for (col <- (dctDataWidth - 1) to 0 by -1) {
          hash <<= 1
          val pixel = imageDataFloat(nextRow)(col)
          if (pixel >= mean) hash |= 1 else hash |= 0
        }
      }
    }
    //debug(s"Computed PHash: $hash from ${dctDataWidth * dctDataHeight} pixels")
    hash
  }
}
