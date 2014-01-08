package com.sothr.imagetools.dto

class ImageHashDTO(val ahash:Long, val dhash:Long, val phash:Long) {

  def getAhash():Long = this.ahash
  def getDhash():Long = this.dhash
  def getPhash():Long = this.phash

  override def toString():String = {
    return s"""ahash: $ahash
    dhash: $dhash
    phash: $phash""".stripMargin
  }
}
