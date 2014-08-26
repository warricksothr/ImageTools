package com.sothr.imagetools.engine.hash

/**
 * Interface for perceptual hashing
 *
 * Created by drew on 1/22/14.
 */
trait PerceptualHasher {

  def getHash(imageData:Array[Array[Int]]):Long

}
