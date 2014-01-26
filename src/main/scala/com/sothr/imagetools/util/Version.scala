package com.sothr.imagetools.util

/**
 * Created by drew on 1/6/14.
 */
class Version(val versionString:String) {
  //parse version into parts
  //typical version string i.e. 0.1.0-DEV-27-060aec7
  val (major,minor,patch,buildTag,buildNumber,buildHash) = {
    val splitVersion = versionString.split("""\.""")
    val splitType = splitVersion(splitVersion.length-1).split("""-""")
    (splitVersion(0).toInt,splitVersion(1).toInt,splitType(0).toInt,splitType(1),splitType(2),splitType(3))
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
