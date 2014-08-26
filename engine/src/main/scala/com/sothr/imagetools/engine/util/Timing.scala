package com.sothr.imagetools.engine.util

import grizzled.slf4j.Logging

trait Timing extends Logging{
 
 def time[R](block: => R): R = {
    val t0 = System.currentTimeMillis
    val result = block    // call-by-name
    val t1 = System.currentTimeMillis
    info("Elapsed time: " + (t1 - t0) + "ms")
    result
  }
  
  def getTime[R](block: => R):Long = {
    val t0 = System.currentTimeMillis
    val result = block    // call-by-name
    val t1 = System.currentTimeMillis
    info("Elapsed time: " + (t1 - t0) + "ms")
    t1 - t0
  }

  def getMean(times:Long*):Long = {
    getMean(times.toArray[Long])
  }

  def getMean(times:Array[Long]):Long = {
    var ag = 0L
    for (i <- times.indices) {
      ag += times(i)
    }
    ag / times.length
  }
    
}