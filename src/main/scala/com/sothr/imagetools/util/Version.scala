package com.sothr.imagetools.util

import grizzled.slf4j.Logging
import java.lang.NumberFormatException

/**
 * Created by drew on 1/6/14.
 */
class Version(val versionString:String) extends Logging{
  //parse version into parts
  //typical version string i.e. 0.1.0-DEV-27-060aec7
  val (major,minor,patch,buildTag,buildNumber,buildHash) = {
    var version:Tuple6[Int,Int,Int,String,Int,String] = (0,0,0,"DEV",0,"asdfzxcv")
    try {
      val splitVersion = versionString.split("""\.""")
      val splitType = splitVersion(splitVersion.length-1).split("""-""")
      version = (splitVersion(0).toInt,splitVersion(1).toInt,splitType(0).toInt,splitType(1),splitType(2).toInt,splitType(3))
    } catch {
      case nfe:NumberFormatException => error(s"Error parsing number from version string '$versionString'", nfe)
      case e:Exception => error(s"Unexpected error parsing version string '$versionString'", e)
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
  def compare(that:Version):Integer = {
    if (this.hashCode == that.hashCode) return 0
    if (this.major > that.major) {
      return 1
    } else if (this.major < that.major){
      return -1
    //major is the same
    } else {
      if (this.minor > that.minor) {
        return 2
      } else if (this.minor < that.minor) {
        return -2
      //major.minor are the same
      } else {
        if (this.patch > that.patch) {
          return 3
        } else if (this.patch < that.patch) {
          return -3
        //major.minor.patch are all the same
        } else {
          if (this.buildTag != that.buildTag) {
            return 4
          }
          //should be caught by the first if, but incase not
          return 0
        }
      }
    }
  }

  def parsableToString():String = {
    s"$major.$minor.$patch-$buildTag-$buildNumber-$buildHash"
  }

  override def toString():String = {
    s"$major.$minor.$patch-$buildTag build:$buildNumber code:$buildHash"
  }
  
  override def hashCode(): Int = {
    val prime:Int = 37;
    val result:Int = 255
    var hash:Int = major
    hash += minor
    hash += patch
    hash += buildTag.hashCode
    return prime * result + hash
  }
}
