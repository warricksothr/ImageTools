package com.sothr.imagetools

/**
 * Created by drew on 1/26/14.
 */
class EngineTest extends BaseTest{
  test("SequentialEngine Test getImagesForDirectory for sample directory") {
    val engine:Engine = new SequentialEngine()
    assertResult(3) { engine.getImagesForDirectory("sample").length }
  }

  test("SequentialEngine Test getSimilarImagesForDirectory for sample directory") {
    val engine = new SequentialEngine()
    val similarImages = engine.getSimilarImagesForDirectory("sample")
    assertResult(1) { similarImages.length }
    assertResult(2) { similarImages(0).similarImages.length }
  }
  
  test("ConcurrentEngine Test getImagesForDirectory for sample directory") {
    val engine:Engine = new ConcurrentEngine()
    assertResult(3) { engine.getImagesForDirectory("sample").length }
  }

  test("ConcurrentEngine Test getSimilarImagesForDirectory for sample directory") {
    val engine = new ConcurrentEngine()
    val similarImages = engine.getSimilarImagesForDirectory("sample")
    assertResult(1) { similarImages.length }
    assertResult(2) { similarImages(0).similarImages.length }
  }
}
