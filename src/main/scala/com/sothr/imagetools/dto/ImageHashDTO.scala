package com.sothr.imagetools.dto

import grizzled.slf4j.Logging

class ImageHashDTO(val ahash:Long, val dhash:Long, val phash:Long) extends Logging {

  def getAhash():Long = this.ahash
  def getDhash():Long = this.dhash
  def getPhash():Long = this.phash

  override def hashCode():Int = {
    var result = 365
    result = 41 * result + (this.ahash ^ (this.ahash >>> 32)).toInt
    result = 37 * result + (this.dhash ^ (this.dhash >>> 32)).toInt
    result = 2 * result + (this.phash ^ (this.phash >>> 32)).toInt
    result
  }

  override def toString():String = {
    s"""ahash: $ahash
    dhash: $dhash
    phash: $phash""".stripMargin
  }
}
