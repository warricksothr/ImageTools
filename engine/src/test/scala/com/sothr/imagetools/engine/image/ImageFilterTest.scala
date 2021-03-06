package com.sothr.imagetools.engine.image

import java.io.File

import com.sothr.imagetools.engine.BaseTest

/**
 * Test to make sure that the image filters work
 *
 * Created by drew on 1/26/14.
 */
class ImageFilterTest extends BaseTest {

  test("Confirm ImageFilter Works") {
    val filter: ImageFilter = new ImageFilter()
    val bogusDirectory = new File(".")
    assert(filter.accept(bogusDirectory, "test.png"))
    assert(filter.accept(bogusDirectory, "test.bmp"))
    assert(filter.accept(bogusDirectory, "test.gif"))
    assert(filter.accept(bogusDirectory, "test.jpg"))
    assert(filter.accept(bogusDirectory, "test.jpeg"))
    assert(filter.accept(bogusDirectory, "test.jpeg.jpg"))
  }

  test("Confirm ImageFiler Fails") {
    val filter: ImageFilter = new ImageFilter()
    val bogusDirectory = new File(".")
    assertResult(false) {
      filter.accept(bogusDirectory, "test")
    }
    assertResult(false) {
      filter.accept(bogusDirectory, "test.mp4")
    }
    assertResult(false) {
      filter.accept(bogusDirectory, "test.gif.mp4")
    }
    assertResult(false) {
      filter.accept(bogusDirectory, "")
    }
  }

}
