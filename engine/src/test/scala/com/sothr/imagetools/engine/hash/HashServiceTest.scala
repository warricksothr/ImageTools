package com.sothr.imagetools.engine.hash

import java.io.File
import javax.imageio.ImageIO

import com.sothr.imagetools.engine.dto.ImageHashDTO
import com.sothr.imagetools.engine.{AppConfig, BaseTest, TestParams}
import net.sf.ehcache.Element

import scala.collection.mutable

/**
 * Test the Hash service and make sure it is consistent
 *
 * Created by dev on 1/23/14.
 */
class HashServiceTest extends BaseTest {

  // Define the number of runs the benchmarking tests should use
  val benchmarkRuns = 10

  def dhashTestCase(filePath: String): Long = {
    val sample = new File(filePath)
    val image = ImageIO.read(sample)
    HashService.getDhash(image)
  }

  test("Benchmark DHash") {
    info("Benchmarking DHash")
    info("DHash Large Image 3684x2736")
    val time = new mutable.MutableList[Long]()
    for (runNum <- 0 until benchmarkRuns) {
      time += getTime {
        dhashTestCase(TestParams.LargeSampleImage1)
      }
    }
    val largeMean = getMean(time.toArray[Long])
    info(s"The mean time of ${time.size} tests for large was: $largeMean ms")
    time.clear()
    info("DHash Medium Image 1824x1368")
    for (runNum <- 0 until benchmarkRuns) {
      time += getTime {
        dhashTestCase(TestParams.MediumSampleImage1)
      }
    }
    val mediumMean = getMean(time.toArray[Long])
    info(s"The mean time of ${time.size} tests for medium was: $mediumMean ms")
    time.clear()
    info("DHash Small Image 912x684")
    for (runNum <- 0 until benchmarkRuns) {
      time += getTime {
        dhashTestCase(TestParams.SmallSampleImage1)
      }
    }
    val smallMean = getMean(time.toArray[Long])
    info(s"The mean time of ${time.size} tests for small was: $smallMean ms")
    time.clear()
    assert(true)
  }

  test("Confirm Largest DHash Output ") {
    val testData: Array[Array[Int]] = Array(
      Array(1, 2, 3, 4, 5, 6, 7, 8),
      Array(16, 15, 14, 13, 12, 11, 10, 9),
      Array(17, 18, 19, 20, 21, 22, 23, 24),
      Array(32, 31, 30, 29, 28, 27, 26, 25),
      Array(33, 34, 35, 36, 37, 38, 39, 40),
      Array(48, 47, 46, 45, 44, 43, 42, 41),
      Array(49, 50, 51, 52, 53, 54, 55, 56),
      Array(64, 63, 62, 61, 60, 59, 58, 57))
    val hash = DHash.getHash(testData)
    debug(s"Hash of test array: $hash")
    assert(hash == Long.MaxValue)
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
    assert(HashService.areDhashSimilar(largeHash, mediumHash))
    assert(HashService.areDhashSimilar(largeHash, smallHash))
    assert(HashService.areDhashSimilar(mediumHash, smallHash))
  }

  def ahashTestCase(filePath: String): Long = {
    val sample = new File(filePath)
    val image = ImageIO.read(sample)
    HashService.getAhash(image)
  }

  test("Benchmark AHash") {
    info("Benchmarking AHash")
    info("AHash Large Image 3684x2736")
    val time = new mutable.MutableList[Long]()
    for (runNum <- 0 until benchmarkRuns) {
      time += getTime {
        ahashTestCase(TestParams.LargeSampleImage1)
      }
    }
    val largeMean = getMean(time.toArray[Long])
    info(s"The mean time of ${time.size} tests for large was: $largeMean ms")
    time.clear()
    info("AHash Medium Image 1824x1368")
    for (runNum <- 0 until benchmarkRuns) {
      time += getTime {
        ahashTestCase(TestParams.MediumSampleImage1)
      }
    }
    val mediumMean = getMean(time.toArray[Long])
    info(s"The mean time of ${time.size} tests for medium was: $mediumMean ms")
    time.clear()
    info("AHash Small Image 912x684")
    for (runNum <- 0 until benchmarkRuns) {
      time += getTime {
        ahashTestCase(TestParams.SmallSampleImage1)
      }
    }
    val smallMean = getMean(time.toArray[Long])
    info(s"The mean time of ${time.size} tests for small was: $smallMean ms")
    time.clear()
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
    assert(HashService.areAhashSimilar(largeHash, mediumHash))
    assert(HashService.areAhashSimilar(largeHash, smallHash))
    assert(HashService.areAhashSimilar(mediumHash, smallHash))
  }

  def phashTestCase(filePath: String): Long = {
    val sample = new File(filePath)
    val image = ImageIO.read(sample)
    HashService.getPhash(image)
  }

  test("Benchmark PHash") {
    info("Benchmarking PHash")
    info("PHash Large Image 3684x2736")
    val time = new mutable.MutableList[Long]()
    for (runNum <- 0 until benchmarkRuns) {
      time += getTime {
        phashTestCase(TestParams.LargeSampleImage1)
      }
    }
    val largeMean = getMean(time.toArray[Long])
    info(s"The mean time of ${time.size} tests for large was: $largeMean ms")
    time.clear()
    info("PHash Medium Image 1824x1368")
    for (runNum <- 0 until benchmarkRuns) {
      time += getTime {
        phashTestCase(TestParams.MediumSampleImage1)
      }
    }
    val mediumMean = getMean(time.toArray[Long])
    info(s"The mean time of ${time.size} tests for medium was: $mediumMean ms")
    time.clear()
    info("PHash Small Image 912x684")
    for (runNum <- 0 until benchmarkRuns) {
      time += getTime {
        phashTestCase(TestParams.SmallSampleImage1)
      }
    }
    val smallMean = getMean(time.toArray[Long])
    info(s"The mean time of ${time.size} tests for small was: $smallMean ms")
    time.clear()
    assert(true)
  }

  test("Calculate PHash Large Sample Image 1") {
    debug("Starting 'Calculate PHash Large Sample Image 1' test")
    val sample = new File(TestParams.LargeSampleImage1)
    debug(s"Testing File: ${sample.getAbsolutePath} exists: ${sample.exists}")
    val image = ImageIO.read(sample)
    debug(s"Image: width: ${image.getWidth} height: ${image.getHeight}")
    val hash = HashService.getPhash(image)
    debug(s"Testing that $hash = -9154554603604154117L")
    assert(hash == -9154554603604154117L)
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
    assert(HashService.arePhashSimilar(largeHash, mediumHash))
    assert(HashService.arePhashSimilar(largeHash, smallHash))
    assert(HashService.arePhashSimilar(mediumHash, smallHash))
  }

  def md5TestCase(filePath: String): String = {
    HashService.getMD5(filePath)
  }

  test("Benchmark MD5") {
    info("Benchmarking MD5")
    info("MD5 Large Image 3684x2736")
    val time = new mutable.MutableList[Long]()
    for (runNum <- 0 until benchmarkRuns) {
      time += getTime {
        md5TestCase(TestParams.LargeSampleImage1)
      }
    }
    val largeMean = getMean(time.toArray[Long])
    info(s"The mean time of ${time.size} tests for large was: $largeMean ms")
    time.clear()
    info("MD5 Medium Image 1824x1368")
    for (runNum <- 0 until benchmarkRuns) {
      time += getTime {
        md5TestCase(TestParams.MediumSampleImage1)
      }
    }
    val mediumMean = getMean(time.toArray[Long])
    info(s"The mean time of ${time.size} tests for medium was: $mediumMean ms")
    time.clear()
    info("MD5 Small Image 912x684")
    for (runNum <- 0 until benchmarkRuns) {
      time += getTime {
        md5TestCase(TestParams.SmallSampleImage1)
      }
    }
    val smallMean = getMean(time.toArray[Long])
    info(s"The mean time of ${time.size} tests for small was: $smallMean ms")
    time.clear()
    assert(true)
  }

  test("Calculate MD5 Large Sample Image 1") {
    debug("Starting 'Calculate MD5 Large Sample Image 1' test")
    val hash = HashService.getMD5(TestParams.LargeSampleImage1)
    debug(s"Testing that $hash = 3fbccfd5faf3f991435b827ee5961862")
    assert(hash == "3fbccfd5faf3f991435b827ee5961862")
  }

  test("Calculate MD5 Medium Sample Image 1") {
    debug("Starting 'Calculate MD5 Medium Sample Image 1' test")
    val hash = HashService.getMD5(TestParams.MediumSampleImage1)
    debug(s"Testing that $hash = a95e2cc4610307eb957e9c812429c53e")
    assert(hash == "a95e2cc4610307eb957e9c812429c53e")
  }

  test("Calculate MD5 Small Sample Image 1") {
    debug("Starting 'Calculate MD5 Small Sample Image 1' test")
    val hash = HashService.getMD5(TestParams.SmallSampleImage1)
    debug(s"Testing that $hash = b137131bd55896c747286e4d247b845e")
    assert(hash == "b137131bd55896c747286e4d247b845e")
  }

  def imageHashTestWithCacheCase(filePath: String): ImageHashDTO = {
    val cache = AppConfig.cacheManager.getCache("images")
    var result: ImageHashDTO = null
    if (cache.get(filePath) != null) {
      result = cache.get(filePath).getObjectValue.asInstanceOf[ImageHashDTO]
    } else {
      result = imageHashTestCase(filePath)
      cache.put(new Element(filePath, result))
    }
    result
  }

  def imageHashTestCase(filePath: String): ImageHashDTO = {
    HashService.getImageHashes(filePath)
  }

  test("Benchmark getImageHashes with cache") {
    info("Benchmarking getImageHashes with cache")
    info("getImageHashes with cache Large Image 3684x2736")
    val time = new mutable.MutableList[Long]()
    for (runNum <- 0 until benchmarkRuns) {
      time += getTime {
        imageHashTestWithCacheCase(TestParams.LargeSampleImage1)
      }
    }
    val largeMean = getMean(time.toArray[Long])
    info(s"The mean time of ${time.size} tests for large was: $largeMean ms")
    time.clear()
    info("getImageHashes with cache Medium Image 1824x1368")
    for (runNum <- 0 until benchmarkRuns) {
      time += getTime {
        imageHashTestWithCacheCase(TestParams.MediumSampleImage1)
      }
    }
    val mediumMean = getMean(time.toArray[Long])
    info(s"The mean time of ${time.size} tests for medium was: $mediumMean ms")
    time.clear()
    info("getImageHashes with cache Small Image 912x684")
    for (runNum <- 0 until benchmarkRuns) {
      time += getTime {
        imageHashTestWithCacheCase(TestParams.SmallSampleImage1)
      }
    }
    val smallMean = getMean(time.toArray[Long])
    info(s"The mean time of ${time.size} tests for small was: $smallMean ms")
    time.clear()
    assert(true)
  }

  test("Benchmark getImageHashes") {
    info("Benchmarking getImageHashes")
    info("getImageHashes Large Image 3684x2736")
    val time = new mutable.MutableList[Long]()
    for (runNum <- 0 until benchmarkRuns) {
      time += getTime {
        imageHashTestCase(TestParams.LargeSampleImage1)
      }
    }
    val largeMean = getMean(time.toArray[Long])
    info(s"The mean time of ${time.size} tests for large was: $largeMean ms")
    time.clear()
    info("getImageHashes Medium Image 1824x1368")
    for (runNum <- 0 until benchmarkRuns) {
      time += getTime {
        imageHashTestCase(TestParams.MediumSampleImage1)
      }
    }
    val mediumMean = getMean(time.toArray[Long])
    info(s"The mean time of ${time.size} tests for medium was: $mediumMean ms")
    time.clear()
    info("getImageHashes Small Image 912x684")
    for (runNum <- 0 until benchmarkRuns) {
      time += getTime {
        imageHashTestCase(TestParams.SmallSampleImage1)
      }
    }
    val smallMean = getMean(time.toArray[Long])
    info(s"The mean time of ${time.size} tests for small was: $smallMean ms")
    time.clear()
    assert(true)
  }

  test("ImageHash Of Large, Medium, And Small Sample 1 Must Be Similar") {
    val largeHash = imageHashTestCase(TestParams.LargeSampleImage1)
    val mediumHash = imageHashTestCase(TestParams.MediumSampleImage1)
    val smallHash = imageHashTestCase(TestParams.SmallSampleImage1)
    assert(HashService.areImageHashesSimilar(largeHash, mediumHash))
    assert(HashService.areImageHashesSimilar(largeHash, smallHash))
    assert(HashService.areImageHashesSimilar(mediumHash, smallHash))
  }

  test("Calculate ImageHash Large Sample Image 1") {
    debug("Starting 'Calculate ImageHash Large Sample Image 1' test")
    val hash = HashService.getImageHashes(TestParams.LargeSampleImage1)
    debug(s"Testing that ${hash.hashCode()} = -812844858")
    assert(hash.hashCode == -812844858)
  }

  test("Calculate ImageHash Medium Sample Image 1") {
    debug("Starting 'Calculate ImageHash Medium Sample Image 1' test")
    val hash = HashService.getImageHashes(TestParams.MediumSampleImage1)
    debug(s"Testing that ${hash.hashCode()} = -812836666")
    assert(hash.hashCode == -812836666)
  }

  test("Calculate ImageHash Small Sample Image 1") {
    debug("Starting 'Calculate ImageHash Small Sample Image 1' test")
    val hash = HashService.getImageHashes(TestParams.SmallSampleImage1)
    debug(s"Testing that ${hash.hashCode()} = -812840762")
    assert(hash.hashCode == -812840762)
  }

}
