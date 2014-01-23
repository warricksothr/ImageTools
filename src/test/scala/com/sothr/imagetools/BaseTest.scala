package com.sothr.imagetools

import org.scalatest.{FunSuite,Matchers,OptionValues,Inside,Inspectors,BeforeAndAfter}

abstract class BaseTest extends FunSuite with Matchers with OptionValues with Inside with Inspectors with BeforeAndAfter {

  before {
    AppConfig.configLogging()
    AppConfig.loadProperties()
  }

}
