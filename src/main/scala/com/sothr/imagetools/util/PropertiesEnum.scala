package com.sothr.imagetools.util

object PropertiesEnum extends Enumeration {
  type PropertiesEnum = Value
  val Version = Value("app.version.current")
  val PreviousVersion = Value("app.version.previous")
  //default app settings
  val Timed = Value("app.timed")
  //default engine concurrency settings
  val ConcurrentSimiliartyLimit = Value("app.engine.concurrent.similarity.limit")
  val ConcurrentProcessingLimit = Value("app.engine.concurrent.processing.limit")
  //default image settings
  val ImageDifferenceThreshold = Value("app.image.differenceThreshold")
  val HashPrecision = Value("app.image.hash.precision")
  val UseAhash = Value("app.image.ahash.use")
  val AhashWeight = Value("app.image.ahash.weight")
  val AhashPrecision = Value("app.image.ahash.precision")
  val AhashTolerance = Value("app.image.ahash.tolerence")
  val UseDhash = Value("app.image.dhash.use")
  val DhashWeight = Value("app.image.dhash.weight")
  val DhashPrecision = Value("app.image.dhash.precision")
  val DhashTolerance = Value("app.image.dhash.tolerence")
  val UsePhash = Value("app.image.phash.use")
  val PhashWeight = Value("app.image.phash.weight")
  val PhashPrecision = Value("app.image.phash.precision")
  val PhashTolerance = Value("app.image.phash.tolerence")
  //Default Thumbnail Settings
  val ThumbnailDirectory = Value("app.thumbnail.directory")
  val ThumbnailSize = Value("app.thumbnail.size")
  //Default Database Settings
  val DatabaseConnectionURL = Value("app.database.connectionURL")
}