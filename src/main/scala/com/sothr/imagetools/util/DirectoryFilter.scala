package com.sothr.imagetools.util

import java.io.{File, FilenameFilter}

/**
 * Created by drew on 1/26/14.
 */
class DirectoryFilter extends FilenameFilter {

  def accept(dir: File, name: String): Boolean = {
    return new File(dir, name).isDirectory();
  }
}
