package com.sothr.imagetools.ui.util

import java.awt.Desktop
import java.io.File

import com.sothr.imagetools.engine.util.PropertiesService
import grizzled.slf4j.Logging

/**
 * Created by Drew Short on 8/31/2014.
 */
object FileUtil extends Logging {

  def openInEditor(file: File) = {
    PropertiesService.OS.toLowerCase match {
      // Open file on windows
      case os if os.startsWith("windows") => openFileWindows(file)
      case os if os.startsWith("linux") => openFileLinux(file)
      case default => error(s"Do not know how to open editor for OS: ${PropertiesService.OS}, ${PropertiesService.OS_VERSION}, ${PropertiesService.OS_ARCH}")
    }
  }

  private def openFileWindows(file: File) = {
    Desktop.getDesktop.open(file)
  }

  private def openFileLinux(file: File) = {
    Runtime.getRuntime.exec(s"xdg-open ${file.getAbsolutePath}")
  }
}
