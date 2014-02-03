package com.sothr.imagetools.dto

import grizzled.slf4j.Logging

class ImageHashDTO(val ahash:Long, val dhash:Long, val phash:Long, val md5:String) extends Serializable with Logging{

  def cloneHashes:ImageHashDTO = {
      return new ImageHashDTO(ahash,dhash,phash,md5)
  }

  override def hashCode():Int = {
    var result = 365
    result = 41 * result + (this.ahash ^ (this.ahash >>> 32)).toInt
    result = 37 * result + (this.dhash ^ (this.dhash >>> 32)).toInt
    result = 2 * result + (this.phash ^ (this.phash >>> 32)).toInt
    result
  }

  override def toString:String = {
    s"MD5: $md5 ahash: $ahash dhash: $dhash phash: $phash"
  }
}
