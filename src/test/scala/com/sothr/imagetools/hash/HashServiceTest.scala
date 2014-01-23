package com.sothr.imagetools.hash

import com.sothr.imagetools.{BaseTest, TestParams}
import javax.imageio.ImageIO
import java.io.File

/**
 * Created by dev on 1/23/14.
 */
class HashServiceTest extends BaseTest {

  def benchmarkDHashTestCase(filePath:String):Long = {
      val sample = new File(filePath)
      val image = ImageIO.read(sample)
      HashService.getDhash(image)
  }

  test("Benchmark DHash") {
      info("Benchmarking DHash")
      info("DHash Large Image 3684x2736")
      val largeTime1 = getTime { benchmarkDHashTestCase(TestParams.LargeSampleImage1) }
      val largeTime2 = getTime { benchmarkDHashTestCase(TestParams.LargeSampleImage1) }
      val largeTime3 = getTime { benchmarkDHashTestCase(TestParams.LargeSampleImage1) }
      val largeTime4 = getTime { benchmarkDHashTestCase(TestParams.LargeSampleImage1) }
      val largeTime5 = getTime { benchmarkDHashTestCase(TestParams.LargeSampleImage1) }
      val largeMean = getMean(largeTime1, largeTime2, largeTime3, largeTime4, largeTime5)
      info(s"The mean time of 5 tests for large was: $largeMean ms")
      info("DHash Medium Image 1824x1368")
      val mediumTime1 = getTime { benchmarkDHashTestCase(TestParams.MediumSampleImage1) }
      val mediumTime2 = getTime { benchmarkDHashTestCase(TestParams.MediumSampleImage1) }
      val mediumTime3 = getTime { benchmarkDHashTestCase(TestParams.MediumSampleImage1) }
      val mediumTime4 = getTime { benchmarkDHashTestCase(TestParams.MediumSampleImage1) }
      val mediumTime5 = getTime { benchmarkDHashTestCase(TestParams.MediumSampleImage1) }
      val mediumMean = getMean(mediumTime1, mediumTime2, mediumTime3, mediumTime4, mediumTime5)
      info(s"The mean time of 5 tests for medium was: $mediumMean ms")
      info("DHash Small Image 912x684")
      val smallTime1 = getTime { benchmarkDHashTestCase(TestParams.SmallSampleImage1) }
      val smallTime2 = getTime { benchmarkDHashTestCase(TestParams.SmallSampleImage1) }
      val smallTime3 = getTime { benchmarkDHashTestCase(TestParams.SmallSampleImage1) }
      val smallTime4 = getTime { benchmarkDHashTestCase(TestParams.SmallSampleImage1) }
      val smallTime5 = getTime { benchmarkDHashTestCase(TestParams.SmallSampleImage1) }
      val smallMean = getMean(smallTime1, smallTime2, smallTime3, smallTime4, smallTime5)
      info(s"The mean time of 5 tests for small was: $smallMean ms")
      assert(true)
  }

  test("Calculate DHash Large Sample Image 1") {
    debug("Starting 'Calculate DHash Large Sample Image 1' test")
    val sample = new File(TestParams.LargeSampleImage1)
    debug(s"Testing File: ${sample.getAbsolutePath} exists: ${sample.exists}")
    val image = ImageIO.read(sample)
    debug(s"Image: width: ${image.getWidth} height: ${image.getHeight}")
    val hash = HashService.getDhash(image)
    debug(s"Testing that $hash = -5198308484644955238L")
    assert(hash == -5198308484644955238L)
  }
  
  test("Calculate DHash Medium Sample Image 1") {
    debug("Starting 'Calculate DHash Medium Sample Image 1' test")
    val sample = new File(TestParams.MediumSampleImage1)
    debug(s"Testing File: ${sample.getAbsolutePath} exists: ${sample.exists}")
    val image = ImageIO.read(sample)
    debug(s"Image: width: ${image.getWidth} height: ${image.getHeight}")
    val hash = HashService.getDhash(image)
    debug(s"Testing that $hash = -5198308484644955238L")
    assert(hash == -5198308484644955238L)
  }
  
  test("Calculate DHash Small Sample Image 1") {
    debug("Starting 'Calculate DHash Small Sample Image 1' test")
    val sample = new File(TestParams.SmallSampleImage1)
    debug(s"Testing File: ${sample.getAbsolutePath} exists: ${sample.exists}")
    val image = ImageIO.read(sample)
    debug(s"Image: width: ${image.getWidth} height: ${image.getHeight}")
    val hash = HashService.getDhash(image)
    debug(s"Testing that $hash = -5198299688551933030L")
    assert(hash == -5198299688551933030L)
  }

}
