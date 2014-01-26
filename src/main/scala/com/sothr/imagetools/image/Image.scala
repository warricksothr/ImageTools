package com.sothr.imagetools.image

import scala.collection.Traversable
import com.sothr.imagetools.dto.ImageHashDTO
import com.sothr.imagetools.hash.HashService

class Image(val imagePath:String, val thumbnailPath:String, var hashes:ImageHashDTO = null) {

  var imageType:ImageType = ImageType.SingleFrameImage

  def isSimilarTo(otherImage:Image):Boolean = {
    HashService.areImageHashesSimilar(this.hashes,otherImage.hashes)
  }

  def getSimilarity(otherImage:Image):Float = {
    HashService.getWeightedHashSimilarity(this.hashes, otherImage.hashes)
  }

  /*def getSimilar(otherImages:Traversable[Image]):Traversable[Image] = {

  }*/

  def getPath:String = {
    this.imagePath
  }

  def getThumbnailPath:String = {
    this.thumbnailPath
  }

  override def toString:String = {
    s"Image: $imagePath Thumbnail: $thumbnailPath Hashes: $hashes"
  }
  
}
