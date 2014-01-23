package com.sothr.imagetools

import grizzled.slf4j.Logging
import com.sothr.imagetools.util.Timing
import org.scalatest.{FunSuite,Matchers,OptionValues,Inside,Inspectors,BeforeAndAfter}

abstract class BaseTest extends FunSuite with Matchers with OptionValues with Inside with Inspectors with BeforeAndAfter with Logging with Timing {

  before {
    AppConfig.configureApp()
  }

}
