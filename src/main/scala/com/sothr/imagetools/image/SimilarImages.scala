package com.sothr.imagetools.image

import grizzled.slf4j.Logging

/**
 * Created by drew on 1/26/14.
 */
class SimilarImages(val rootImage:Image, val similarImages:List[Image]) extends Logging {

  protected def getPrettySimilarImagesList:String = {
    val sb = new StringBuilder()
    for (image <- similarImages) {
      sb.append(image.imagePath)
      sb.append(System.lineSeparator())
    }
    sb.toString()
  }

  override def toString:String = {
    s"""RootImage: ${rootImage.imagePath}
    Similar Images:
    ${getPrettySimilarImagesList}""".stripMargin
  }

}
