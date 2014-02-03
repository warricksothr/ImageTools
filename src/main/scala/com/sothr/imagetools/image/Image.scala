package com.sothr.imagetools.image

import com.sothr.imagetools.dto.ImageHashDTO
import com.sothr.imagetools.hash.HashService
import grizzled.slf4j.Logging

class Image(val imagePath:String, val thumbnailPath:String, val imageSize:Tuple2[Int,Int], var hashes:ImageHashDTO = null) extends Serializable with Logging{

  var imageType:ImageType = ImageType.SingleFrameImage

  def isSimilarTo(otherImage:Image):Boolean = {
    //debug(s"Checking $imagePath for similarities with ${otherImage.imagePath}")
    HashService.areImageHashesSimilar(this.hashes,otherImage.hashes)
  }

  def getSimilarity(otherImage:Image):Float = {
    HashService.getWeightedHashSimilarity(this.hashes, otherImage.hashes)
  }

  /*def getSimilar(otherImages:Traversable[Image]):Traversable[Image] = {

  }*/

  def cloneImage:Image = {
      return new Image(imagePath,thumbnailPath,imageSize,hashes.cloneHashes)
  }

  override def toString:String = {
    s"Image: $imagePath Thumbnail: $thumbnailPath Image Size: ${imageSize._1}x${imageSize._2} Hashes: $hashes"
  }

  override def equals(obj:Any) = {
      obj match {
          case that:Image =>
            that.hashCode.equals(this.hashCode)
          case _ => false
      }
  }

  override def hashCode:Int = {
    var result = 365
    result = 37 * result + imagePath.hashCode
    result = 41 * result + hashes.hashCode()
    result
  }
  
}
