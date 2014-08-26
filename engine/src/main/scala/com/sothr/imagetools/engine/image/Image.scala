package com.sothr.imagetools.engine.image

import javax.persistence._

import com.sothr.imagetools.engine.dto.ImageHashDTO
import com.sothr.imagetools.engine.hash.HashService
import grizzled.slf4j.Logging

@Entity
@Table(name = "Image")
class Image(val image:String, val thumbnail:String, val size:(Int, Int), val imageHashes:ImageHashDTO = null) extends Serializable with Logging {

  def this() = this ("", "", (0,0), null)

  @Id
  var imagePath:String = image
  def getImagePath:String = imagePath
  def setImagePath(path:String) = { imagePath = path}
  var thumbnailPath:String = thumbnail
  def getThumbnailPath:String = thumbnailPath
  def setThumbnailPath(path:String) = { thumbnailPath = path}
  var width:Int = size._1
  def getWidth:Int = width
  def setWidth(size:Int) = { width = size}
  var height:Int = size._2
  def getHeight:Int = height
  def setHeight(size:Int) = { height = size}
  var hashes:ImageHashDTO = imageHashes
  def getHashes:ImageHashDTO = hashes
  def setHashes(newHashes:ImageHashDTO) = { hashes = newHashes}

  @transient
  var imageSize:(Int, Int) = { new Tuple2(width, height) }

  @transient
  var imageName:String = ""

  var imageType:ImageType = ImageType.SingleFrameImage

  def getName:String = {
    if(this.imageName.length < 1) {
      this.imageName = this.getImagePath.split('/').last
    }
    this.imageName
  }

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
      new Image(imagePath,thumbnailPath,imageSize,hashes.cloneHashes)
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
