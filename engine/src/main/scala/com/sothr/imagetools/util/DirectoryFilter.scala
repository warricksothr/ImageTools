package com.sothr.imagetools.util

import java.io.{File, FilenameFilter}

/**
 * Filter directories
 *
 * Created by drew on 1/26/14.
 */
class DirectoryFilter extends FilenameFilter {

  def accept(dir: File, name: String): Boolean = {
    new File(dir, name).isDirectory
  }
}
