package com.sothr.imagetools

/**
 * Created by drew on 1/26/14.
 */
class EngineTest extends BaseTest{
  test("Test getImagesForDirectory for sample directory") {
    val engine:Engine = new Engine()
    assertResult(3) { engine.getImagesForDirectory("sample").length }
  }
}
