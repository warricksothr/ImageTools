package com.sothr.imagetools.engine

import com.sothr.imagetools.engine.util.Timing
import grizzled.slf4j.Logging
import org.scalatest.{BeforeAndAfter, FunSuite, Inside, Inspectors, Matchers, OptionValues}

abstract class BaseTest extends FunSuite with Matchers with OptionValues with Inside with Inspectors with BeforeAndAfter with Logging with Timing {

  before {
    AppConfig.configureApp()
  }

}
