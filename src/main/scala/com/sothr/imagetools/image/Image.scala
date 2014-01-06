package com.sothr.imagetools.image

import scala.collection.Traversable
import com.sothr.imagetools.dto.ImageHashDTO

abstract class Image(val imagePath:String, val thumbnailPath:String, protected var hashes:ImageHashDTO = null) {

  protected val imageType:ImageType = ImageType.SingleFrameImage

  def getHashes():ImageHashDTO = this.hashes
  def setHashes(newHashes:ImageHashDTO) = { this.hashes = newHashes }

  def isSimilarTo(otherImage:Image):Boolean

  def getSimilarity(otherImage:Image)

  def getSimilar(otherImages:Traversable[Image]):Traversable[Image]

  def getPath():String = {
    return this.imagePath
  }

  def getThumbnailPath():String = {
    return this.thumbnailPath
  }
  
}
