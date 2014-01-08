package com.sothr.imagetools.util

/**
 * Created by drew on 1/6/14.
 */
class Version(val versionString:String) {
  //parse version into parts
  val (major,minor,revision,buildType) = {
    val splitVersion = versionString.split("""\.""")
    val splitType = splitVersion(splitVersion.length-1).split("""-""")
    (splitVersion(0).toInt,splitVersion(1).toInt,splitType(0).toInt,splitType(1))
  }

  /*
  * -3 = this.revision < that.revision
  * -2 = this.minor < that.minor
  * -1 = this.major < that.major
  * 0 = Identical Versions
  * 1 = this.major > that.major
  * 2 = this.minor > that.minor
  * 3 = this.revision > that.revision
  * 4 = this.buildType != that.buildType
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
        if (this.revision > that.revision) {
          return 3
        } else if (this.revision < that.revision) {
          return -3
        //major.minor.revision are all the same
        } else {
          if (this.buildType != that.buildType) {
            return 4
          }
          //should be caught by the first if, but incase not
          return 0
        }
      }
    }
  }

  override def toString():String = {
    return s"$major.$minor.$revision-$buildType"
  }
  
  override def hashCode(): Int = {
    val prime:Int = 37;
    val result:Int = 255
    var hash:Int = major
    hash += minor
    hash += revision
    hash += buildType.hashCode
    return prime * result + hash
  }
}
