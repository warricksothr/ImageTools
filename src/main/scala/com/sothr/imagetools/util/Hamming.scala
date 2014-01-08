package com.sothr.imagetools.util

object Hamming {

  /**
   * Calculate the hamming distance between two longs
   *
   * @param hash1 The first hash to compare
   * @param hash2 The second hash to compare
   * @return
   */
  def getDistance(hash1: Long, hash2: Long): Int = {
    //The XOR of hash1 and hash2 is converted to a binary string
    //then the number of '1's is counted. This is the hamming distance
    (hash1 ^ hash2).toBinaryString.count(_ == '1')
  }

}