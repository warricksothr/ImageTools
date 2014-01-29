package com.sothr.imagetools.util

object PropertiesEnum extends Enumeration {
  type PropertiesEnum = Value
  val Version = Value("version")
  //default app settings
  val LogDebug = Value("app.log.debug")
  val LogInfo = Value("app.log.info")
  val LogError = Value("app.log.error")
  val Timed = Value("app.timed")
  //default engine concurrency settings
  val ConcurrentSimiliartyLimit = Value("app.engine.concurrent.similarity.limit")
  val ConcurrentProcessingLimit = Value("app.engine.concurrent.processing.limit")
  //default image settings
  val ImageDifferenceThreshold = Value("image.differenceThreshold")
  val HashPrecision = Value("image.hash.precision")
  val UseAhash = Value("image.ahash.use")
  val AhashWeight = Value("image.ahash.weight")
  val AhashPrecision = Value("image.ahash.precision")
  val AhashTolerance = Value("image.ahash.tolerence")
  val UseDhash = Value("image.dhash.use")
  val DhashWeight = Value("image.dhash.weight")
  val DhashPrecision = Value("image.dhash.precision")
  val DhashTolerance = Value("image.dhash.tolerence")
  val UsePhash = Value("image.phash.use")
  val PhashWeight = Value("image.phash.weight")
  val PhashPrecision = Value("image.phash.precision")
  val PhashTolerance = Value("image.phash.tolerence")
  //Default Thumbnail Settings
  val ThumbnailDirectory = Value("thumbnail.directory")
  val ThumbnailSize = Value("thumbnail.size")
}