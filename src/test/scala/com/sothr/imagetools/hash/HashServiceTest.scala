package com.sothr.imagetools.hash

import com.sothr.imagetools.{BaseTest, TestParams}
import javax.imageio.ImageIO
import java.io.File

/**
 * Created by dev on 1/23/14.
 */
class HashServiceTest extends BaseTest {

  def dhashTestCase(filePath:String):Long = {
      val sample = new File(filePath)
      val image = ImageIO.read(sample)
      HashService.getDhash(image)
  }

  test("Benchmark DHash") {
      info("Benchmarking DHash")
      info("DHash Large Image 3684x2736")
      val largeTime1 = getTime { dhashTestCase(TestParams.LargeSampleImage1) }
      val largeTime2 = getTime { dhashTestCase(TestParams.LargeSampleImage1) }
      val largeTime3 = getTime { dhashTestCase(TestParams.LargeSampleImage1) }
      val largeTime4 = getTime { dhashTestCase(TestParams.LargeSampleImage1) }
      val largeTime5 = getTime { dhashTestCase(TestParams.LargeSampleImage1) }
      val largeMean = getMean(largeTime1, largeTime2, largeTime3, largeTime4, largeTime5)
      info(s"The mean time of 5 tests for large was: $largeMean ms")
      info("DHash Medium Image 1824x1368")
      val mediumTime1 = getTime { dhashTestCase(TestParams.MediumSampleImage1) }
      val mediumTime2 = getTime { dhashTestCase(TestParams.MediumSampleImage1) }
      val mediumTime3 = getTime { dhashTestCase(TestParams.MediumSampleImage1) }
      val mediumTime4 = getTime { dhashTestCase(TestParams.MediumSampleImage1) }
      val mediumTime5 = getTime { dhashTestCase(TestParams.MediumSampleImage1) }
      val mediumMean = getMean(mediumTime1, mediumTime2, mediumTime3, mediumTime4, mediumTime5)
      info(s"The mean time of 5 tests for medium was: $mediumMean ms")
      info("DHash Small Image 912x684")
      val smallTime1 = getTime { dhashTestCase(TestParams.SmallSampleImage1) }
      val smallTime2 = getTime { dhashTestCase(TestParams.SmallSampleImage1) }
      val smallTime3 = getTime { dhashTestCase(TestParams.SmallSampleImage1) }
      val smallTime4 = getTime { dhashTestCase(TestParams.SmallSampleImage1) }
      val smallTime5 = getTime { dhashTestCase(TestParams.SmallSampleImage1) }
      val smallMean = getMean(smallTime1, smallTime2, smallTime3, smallTime4, smallTime5)
      info(s"The mean time of 5 tests for small was: $smallMean ms")
      assert(true)
  }

  test("Confirm Largest DHash Output ") {
    val testData:Array[Array[Int]] = Array(
      Array(1,2,3,4,5,6,7,8),
      Array(16,15,14,13,12,11,10,9),
      Array(17,18,19,20,21,22,23,24),
      Array(32,31,30,29,28,27,26,25),
      Array(33,34,35,36,37,38,39,40),
      Array(48,47,46,45,44,43,42,41),
      Array(49,50,51,52,53,54,55,56),
      Array(64,63,62,61,60,59,58,57))
    val hash = DHash.getHash(testData)
    debug(s"Hash of test array: $hash")
    assert(hash == (Long.MaxValue))
  }

  test("Calculate DHash Large Sample Image 1") {
    debug("Starting 'Calculate DHash Large Sample Image 1' test")
    val sample = new File(TestParams.LargeSampleImage1)
    debug(s"Testing File: ${sample.getAbsolutePath} exists: ${sample.exists}")
    val image = ImageIO.read(sample)
    debug(s"Image: width: ${image.getWidth} height: ${image.getHeight}")
    val hash = HashService.getDhash(image)
    debug(s"Testing that $hash = 4004374827879799635L")
    assert(hash == 4004374827879799635L)
  }
  
  test("Calculate DHash Medium Sample Image 1") {
    debug("Starting 'Calculate DHash Medium Sample Image 1' test")
    val sample = new File(TestParams.MediumSampleImage1)
    debug(s"Testing File: ${sample.getAbsolutePath} exists: ${sample.exists}")
    val image = ImageIO.read(sample)
    debug(s"Image: width: ${image.getWidth} height: ${image.getHeight}")
    val hash = HashService.getDhash(image)
    debug(s"Testing that $hash = 4004374827879799635L")
    assert(hash == 4004374827879799635L)
  }
  
  test("Calculate DHash Small Sample Image 1") {
    debug("Starting 'Calculate DHash Small Sample Image 1' test")
    val sample = new File(TestParams.SmallSampleImage1)
    debug(s"Testing File: ${sample.getAbsolutePath} exists: ${sample.exists}")
    val image = ImageIO.read(sample)
    debug(s"Image: width: ${image.getWidth} height: ${image.getHeight}")
    val hash = HashService.getDhash(image)
    debug(s"Testing that $hash = 4004383623972821843L")
    assert(hash == 4004383623972821843L)
  }
  
  test("DHash Of Large, Medium, And Small Sample 1 Must Be Similar") {
    val largeHash = dhashTestCase(TestParams.LargeSampleImage1)
    val mediumHash = dhashTestCase(TestParams.MediumSampleImage1)
    val smallHash = dhashTestCase(TestParams.SmallSampleImage1)
    assert(HashService.areDhashSimilar(largeHash,mediumHash))
    assert(HashService.areDhashSimilar(largeHash,smallHash))
    assert(HashService.areDhashSimilar(mediumHash,smallHash))
  }

  def ahashTestCase(filePath:String):Long = {
    val sample = new File(filePath)
    val image = ImageIO.read(sample)
    HashService.getAhash(image)
  }

  test("Benchmark AHash") {
    info("Benchmarking AHash")
    info("AHash Large Image 3684x2736")
    val largeTime1 = getTime { ahashTestCase(TestParams.LargeSampleImage1) }
    val largeTime2 = getTime { ahashTestCase(TestParams.LargeSampleImage1) }
    val largeTime3 = getTime { ahashTestCase(TestParams.LargeSampleImage1) }
    val largeTime4 = getTime { ahashTestCase(TestParams.LargeSampleImage1) }
    val largeTime5 = getTime { ahashTestCase(TestParams.LargeSampleImage1) }
    val largeMean = getMean(largeTime1, largeTime2, largeTime3, largeTime4, largeTime5)
    info(s"The mean time of 5 tests for large was: $largeMean ms")
    info("AHash Medium Image 1824x1368")
    val mediumTime1 = getTime { ahashTestCase(TestParams.MediumSampleImage1) }
    val mediumTime2 = getTime { ahashTestCase(TestParams.MediumSampleImage1) }
    val mediumTime3 = getTime { ahashTestCase(TestParams.MediumSampleImage1) }
    val mediumTime4 = getTime { ahashTestCase(TestParams.MediumSampleImage1) }
    val mediumTime5 = getTime { ahashTestCase(TestParams.MediumSampleImage1) }
    val mediumMean = getMean(mediumTime1, mediumTime2, mediumTime3, mediumTime4, mediumTime5)
    info(s"The mean time of 5 tests for medium was: $mediumMean ms")
    info("AHash Small Image 912x684")
    val smallTime1 = getTime { ahashTestCase(TestParams.SmallSampleImage1) }
    val smallTime2 = getTime { ahashTestCase(TestParams.SmallSampleImage1) }
    val smallTime3 = getTime { ahashTestCase(TestParams.SmallSampleImage1) }
    val smallTime4 = getTime { ahashTestCase(TestParams.SmallSampleImage1) }
    val smallTime5 = getTime { ahashTestCase(TestParams.SmallSampleImage1) }
    val smallMean = getMean(smallTime1, smallTime2, smallTime3, smallTime4, smallTime5)
    info(s"The mean time of 5 tests for small was: $smallMean ms")
    assert(true)
  }

  test("Calculate AHash Large Sample Image 1") {
    debug("Starting 'Calculate AHash Large Sample Image 1' test")
    val sample = new File(TestParams.LargeSampleImage1)
    debug(s"Testing File: ${sample.getAbsolutePath} exists: ${sample.exists}")
    val image = ImageIO.read(sample)
    debug(s"Image: width: ${image.getWidth} height: ${image.getHeight}")
    val hash = HashService.getAhash(image)
    debug(s"Testing that $hash = 36070299219713907L")
    assert(hash == 36070299219713907L)
  }

  test("Calculate AHash Medium Sample Image 1") {
    debug("Starting 'Calculate AHash Medium Sample Image 1' test")
    val sample = new File(TestParams.MediumSampleImage1)
    debug(s"Testing File: ${sample.getAbsolutePath} exists: ${sample.exists}")
    val image = ImageIO.read(sample)
    debug(s"Image: width: ${image.getWidth} height: ${image.getHeight}")
    val hash = HashService.getAhash(image)
    debug(s"Testing that $hash = 36070299219713907L")
    assert(hash == 36070299219713907L)
  }

  test("Calculate AHash Small Sample Image 1") {
    debug("Starting 'Calculate AHash Small Sample Image 1' test")
    val sample = new File(TestParams.SmallSampleImage1)
    debug(s"Testing File: ${sample.getAbsolutePath} exists: ${sample.exists}")
    val image = ImageIO.read(sample)
    debug(s"Image: width: ${image.getWidth} height: ${image.getHeight}")
    val hash = HashService.getAhash(image)
    debug(s"Testing that $hash = 36070299219713907L")
    assert(hash == 36070299219713907L)
  }

  test("AHash Of Large, Medium, And Small Sample 1 Must Be Similar") {
    val largeHash = ahashTestCase(TestParams.LargeSampleImage1)
    val mediumHash = ahashTestCase(TestParams.MediumSampleImage1)
    val smallHash = ahashTestCase(TestParams.SmallSampleImage1)
    assert(HashService.areAhashSimilar(largeHash,mediumHash))
    assert(HashService.areAhashSimilar(largeHash,smallHash))
    assert(HashService.areAhashSimilar(mediumHash,smallHash))
  }

  def phashTestCase(filePath:String):Long = {
    val sample = new File(filePath)
    val image = ImageIO.read(sample)
    HashService.getPhash(image)
  }

  test("Benchmark PHash") {
    info("Benchmarking PHash")
    info("PHash Large Image 3684x2736")
    val largeTime1 = getTime { phashTestCase(TestParams.LargeSampleImage1) }
    val largeTime2 = getTime { phashTestCase(TestParams.LargeSampleImage1) }
    val largeTime3 = getTime { phashTestCase(TestParams.LargeSampleImage1) }
    val largeTime4 = getTime { phashTestCase(TestParams.LargeSampleImage1) }
    val largeTime5 = getTime { phashTestCase(TestParams.LargeSampleImage1) }
    val largeMean = getMean(largeTime1, largeTime2, largeTime3, largeTime4, largeTime5)
    info(s"The mean time of 5 tests for large was: $largeMean ms")
    info("PHash Medium Image 1824x1368")
    val mediumTime1 = getTime { phashTestCase(TestParams.MediumSampleImage1) }
    val mediumTime2 = getTime { phashTestCase(TestParams.MediumSampleImage1) }
    val mediumTime3 = getTime { phashTestCase(TestParams.MediumSampleImage1) }
    val mediumTime4 = getTime { phashTestCase(TestParams.MediumSampleImage1) }
    val mediumTime5 = getTime { phashTestCase(TestParams.MediumSampleImage1) }
    val mediumMean = getMean(mediumTime1, mediumTime2, mediumTime3, mediumTime4, mediumTime5)
    info(s"The mean time of 5 tests for medium was: $mediumMean ms")
    info("PHash Small Image 912x684")
    val smallTime1 = getTime { phashTestCase(TestParams.SmallSampleImage1) }
    val smallTime2 = getTime { phashTestCase(TestParams.SmallSampleImage1) }
    val smallTime3 = getTime { phashTestCase(TestParams.SmallSampleImage1) }
    val smallTime4 = getTime { phashTestCase(TestParams.SmallSampleImage1) }
    val smallTime5 = getTime { phashTestCase(TestParams.SmallSampleImage1) }
    val smallMean = getMean(smallTime1, smallTime2, smallTime3, smallTime4, smallTime5)
    info(s"The mean time of 5 tests for small was: $smallMean ms")
    assert(true)
  }

  test("Calculate PHash Large Sample Image 1") {
    debug("Starting 'Calculate PHash Large Sample Image 1' test")
    val sample = new File(TestParams.LargeSampleImage1)
    debug(s"Testing File: ${sample.getAbsolutePath} exists: ${sample.exists}")
    val image = ImageIO.read(sample)
    debug(s"Image: width: ${image.getWidth} height: ${image.getHeight}")
    val hash = HashService.getPhash(image)
    debug(s"Testing that $hash = -9154589787976242949L")
    assert(hash == -9154589787976242949L)
  }

  test("Calculate PHash Medium Sample Image 1") {
    debug("Starting 'Calculate PHash Medium Sample Image 1' test")
    val sample = new File(TestParams.MediumSampleImage1)
    debug(s"Testing File: ${sample.getAbsolutePath} exists: ${sample.exists}")
    val image = ImageIO.read(sample)
    debug(s"Image: width: ${image.getWidth} height: ${image.getHeight}")
    val hash = HashService.getPhash(image)
    debug(s"Testing that $hash = -9154589787976242949L")
    assert(hash == -9154589787976242949L)
  }

  test("Calculate PHash Small Sample Image 1") {
    debug("Starting 'Calculate PHash Small Sample Image 1' test")
    val sample = new File(TestParams.SmallSampleImage1)
    debug(s"Testing File: ${sample.getAbsolutePath} exists: ${sample.exists}")
    val image = ImageIO.read(sample)
    debug(s"Image: width: ${image.getWidth} height: ${image.getHeight}")
    val hash = HashService.getPhash(image)
    debug(s"Testing that $hash = -9154589787976242949L")
    assert(hash == -9154589787976242949L)
  }

  test("PHash Of Large, Medium, And Small Sample 1 Must Be Similar") {
    val largeHash = phashTestCase(TestParams.LargeSampleImage1)
    val mediumHash = phashTestCase(TestParams.MediumSampleImage1)
    val smallHash = phashTestCase(TestParams.SmallSampleImage1)
    assert(HashService.arePhashSimilar(largeHash,mediumHash))
    assert(HashService.arePhashSimilar(largeHash,smallHash))
    assert(HashService.arePhashSimilar(mediumHash,smallHash))
  }

}
