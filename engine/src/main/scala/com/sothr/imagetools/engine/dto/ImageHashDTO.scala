package com.sothr.imagetools.engine.dto

import javax.persistence._

import grizzled.slf4j.Logging

@Entity
@Table(name = "ImageHash")
class ImageHashDTO(var ahash: Long, var dhash: Long, var phash: Long, var md5: String) extends Serializable with Logging {

  def this() = this(0l, 0l, 0l, "")

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  var id: Int = _

  def getId: Int = id

  def setId(newId: Int) = {
    id = newId
  }

  def getAhash: Long = ahash

  def setAhash(hash: Long) = {
    ahash = hash
  }

  def getDhash: Long = dhash

  def setDhash(hash: Long) = {
    dhash = hash
  }

  def getPhash: Long = phash

  def setPhash(hash: Long) = {
    phash = hash
  }

  def getMd5: String = md5

  def setMd5(hash: String) = {
    md5 = hash
  }

  def cloneHashes: ImageHashDTO = {
    new ImageHashDTO(ahash, dhash, phash, md5)
  }

  override def hashCode(): Int = {
    var result = 365
    result = 41 * result + (this.ahash ^ (this.ahash >>> 32)).toInt
    result = 37 * result + (this.dhash ^ (this.dhash >>> 32)).toInt
    result = 2 * result + (this.phash ^ (this.phash >>> 32)).toInt
    result
  }

  override def toString: String = {
    s"MD5: $md5 ahash: $ahash dhash: $dhash phash: $phash"
  }
}
