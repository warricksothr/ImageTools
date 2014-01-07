package com.sothr.imagetools.util

/**
 * Created by drew on 1/6/14.
 */
class Version(val versionString:String) {
  //parse version into parts
  val (major,minor,revision,buildType) = {
    val splitVersion = versionString.split("""\.""")
    val splitType = splitVersion(splitVersion.length-1).split("""-""")
    (splitVersion(0),splitVersion(1),splitType(0),splitType(1))
  }

  override def toString():String = {
    return s"$major.$minor.$revision-$buildType"
  }
}
