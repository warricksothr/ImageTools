package com.sothr.imagetools.engine.image

import java.io.{File, FilenameFilter}

import scala.collection.immutable.HashSet

/**
 * Filter for file names
 *
 * Used to detect image files based on extension
 *
 * Created by drew on 1/26/14.
 */
class ImageFilter extends FilenameFilter {

  private val extensions:HashSet[String] = new HashSet[String]() ++ Array("png", "bmp", "gif", "jpg", "jpeg")

  def accept(dir: File, name: String): Boolean = {
    val splitName = name.split('.')
    val extension = if (splitName.length > 1) splitName(splitName.length-1) else ""
    if (extensions.contains(extension)) true else false
  }
}
