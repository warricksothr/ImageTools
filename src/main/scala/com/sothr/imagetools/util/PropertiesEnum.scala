package com.sothr.imagetools.util

object PropertiesEnum extends Enumeration {
  type PropertiesEnum = Value
  val Version = Value("version")
  //default image settings
  val ImageDifferenceThreshold = Value("image.differenceThreshold")
  val UseAhash = Value("image.ahash.use")
  val AhashWeight = Value("image.ahash.weight")
  val UseDhash = Value("image.dhash.use")
  val DhashWeight = Value("image.dhash.weight")
  val UsePhash = Value("image.phash.use")
  val PhashWeight = Value("image.phash.weight")
  //Default Thumbnail Settings
  val ThumbnailDirectory = Value("thumbnail.directory")
  val ThumbnailSize = Value("thumbnail.size")
}