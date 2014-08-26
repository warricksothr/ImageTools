package com.sothr.imagetools.engine.util

import grizzled.slf4j.Logging

/**
 * Class to handle version detection and evaluation
 *
 * Created by drew on 1/6/14.
 */
class Version(val versionString: String) extends Logging {
  //parse version into parts
  //typical version string i.e. 0.1.0-DEV-27-060aec7
  val (major, minor, patch, buildTag, buildNumber, buildHash) = {
    var version: (Int, Int, Int, String, Int, String) = (0, 0, 0, "DEV", 0, "asdfzxcv")
    try {
      val splitVersion = versionString.split( """\.""")
      val splitType = splitVersion(splitVersion.length - 1).split( """-""")
      version = (splitVersion(0).toInt, splitVersion(1).toInt, splitType(0).toInt, splitType(1), splitType(2).toInt, splitType(3))
    } catch {
      case nfe: NumberFormatException => error(s"Error parsing number from version string '$versionString'", nfe)
      case e: Exception => error(s"Unexpected error parsing version string '$versionString'", e)
    }
    version
  }

  /*
  * -3 = this.patch < that.patch
  * -2 = this.minor < that.minor
  * -1 = this.major < that.major
  * 0 = Identical Versions
  * 1 = this.major > that.major
  * 2 = this.minor > that.minor
  * 3 = this.patch > that.patch
  * 4 = this.buildTag != that.buildTag
  */
  def compare(that: Version): Integer = {
    //Identical Versions
    if (this.hashCode == that.hashCode) {
      0
      // This is at least a major version ahead
    } else if (this.major > that.major) {
      1
      // This is at least a major version behind
    } else if (this.major < that.major) {
      -1
      // major is the same
    } else {
      // This is at least a minor version ahead
      if (this.minor > that.minor) {
        2
        // This is at least a minor version behind
      } else if (this.minor < that.minor) {
        -2
        // major.minor are the same
      } else {
        // This is at least a patch version ahead
        if (this.patch > that.patch) {
          3
          // This is at least a patch version version
        } else if (this.patch < that.patch) {
          -3
          //major.minor.patch are all the same
        } else {
          // This is a different build
          if (this.buildTag != that.buildTag) {
            4
          }
          //should be caught by the first if, but in case not
          0
        }
      }
    }
  }

  def parsableToString(): String = {
    s"$major.$minor.$patch-$buildTag-$buildNumber-$buildHash"
  }

  override def toString: String = {
    s"$major.$minor.$patch-$buildTag build:$buildNumber code:$buildHash"
  }

  override def hashCode(): Int = {
    val prime: Int = 37
    val result: Int = 255
    var hash: Int = major
    hash += minor
    hash += patch
    hash += buildTag.hashCode
    prime * result + hash
  }
}
