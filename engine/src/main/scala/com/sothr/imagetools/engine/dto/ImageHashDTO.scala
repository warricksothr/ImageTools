package com.sothr.imagetools.engine.dto

import javax.persistence._

import grizzled.slf4j.Logging

@Entity
@Table(name = "ImageHash")
class ImageHashDTO(var ahash: Long, var dhash: Long, var phash: Long, var fileHash: String) extends Serializable with Logging {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  var id: Int = _

  def this() = this(0l, 0l, 0l, "")

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

  def getFileHash: String = fileHash

  def setFileHash(hash: String) = {
    fileHash = hash
  }

  def cloneHashes: ImageHashDTO = {
    new ImageHashDTO(ahash, dhash, phash, fileHash)
  }

  override def hashCode(): Int = {
    var result = 365
    result = 41 * result + (this.ahash ^ (this.ahash >>> 32)).toInt
    result = 37 * result + (this.dhash ^ (this.dhash >>> 32)).toInt
    result = 2 * result + (this.phash ^ (this.phash >>> 32)).toInt
    result
  }

  override def toString: String = {
    s"fileHash: $fileHash ahash: $ahash dhash: $dhash phash: $phash"
  }
}
