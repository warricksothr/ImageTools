package com.sothr.imagetools.engine.image

import grizzled.slf4j.Logging

/**
 * Similar Image payload class
 *
 * Created by drew on 1/26/14.
 */
class SimilarImages(var similarImages: Set[Image]) extends Logging {

  override def hashCode: Int = {
    val prime = 7
    var result = prime * similarImages.size
    for (similarImage <- similarImages) {
      result = prime * result + similarImage.hashCode
    }
    result
  }

  override def toString: String = {
    s"""Similar Images:
    $getPrettySimilarImagesList""".stripMargin
  }

  protected def getPrettySimilarImagesList: String = {
    val sb = new StringBuilder()
    for (image <- similarImages) {
      sb.append(image.imagePath)
      sb.append(System.lineSeparator())
    }
    sb.toString()
  }

  def ordering() = {
    1 * similarImages.size
  }

}

object SimilarImagesOrdering extends Ordering[SimilarImages] {
  def compare(a:SimilarImages, b:SimilarImages) = a.ordering() compare b.ordering()
}
