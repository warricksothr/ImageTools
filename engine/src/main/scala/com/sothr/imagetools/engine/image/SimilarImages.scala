package com.sothr.imagetools.engine.image

import grizzled.slf4j.Logging

/**
 * Similar Image payload class
 *
 * Created by drew on 1/26/14.
 */
class SimilarImages(val rootImage: Image, val similarImages: List[Image]) extends Logging {

  override def hashCode: Int = {
    val prime = 7
    var result = prime * 1 + rootImage.hashCode
    for (similarImage <- similarImages) {
      result = prime * result + similarImage.hashCode
    }
    result
  }

  override def toString: String = {
    s"""RootImage: ${rootImage.imagePath}
    Similar Images:
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

}
