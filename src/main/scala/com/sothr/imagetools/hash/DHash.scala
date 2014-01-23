package com.sothr.imagetools.hash

/**
 * Created by dev on 1/22/14.
 */
object DHash extends PerceptualHasher {
  def getHash(imageData: Array[Array[Int]]): Long = {
    val width = imageData.length
    val height = imageData(0).length
    var hash = 0L
    for (row <- 0 until width) {
      //println(f"Row: $row%d")
      var previousPixel = imageData(row)(0)
      var previousLocation = (row, 0)

      //process each column
      for (col <- 0 until height) {
        //println(f"Column: $col%d")
        hash <<= 1
        val pixel = imageData(row)(col)
        //binary or the current bit based on whether the value
        //of the current pixel is greater or equal to the previous pixel
        if (pixel >= previousPixel) hash |= 1 else hash |= 0
        previousPixel = pixel
        previousLocation = (row, col)
      }
    }
    hash
  }
}
