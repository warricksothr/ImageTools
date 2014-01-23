package com.sothr.imagetools.hash

/**
 * Created by dev on 1/22/14.
 */
trait PerceptualHasher {

  def getHash(imageData:Array[Array[Int]]):Long

}
