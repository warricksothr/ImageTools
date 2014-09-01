package com.sothr.imagetools.engine.util

import java.io.{File, FileOutputStream, PrintStream}
import java.util.Properties

import com.typesafe.config.{Config, ConfigFactory, ConfigRenderOptions}
import grizzled.slf4j.Logging

/*
 * Service for loading and interacting with the properties file
 */
object PropertiesService extends Logging {

  private var defaultConf: Config = null
  private var userConf: Config = null
  private val newUserConf: Properties = new Properties()
  private var version: Version = null
  private val configRenderOptions = ConfigRenderOptions.concise().setFormatted(true)

  def getVersion: Version = this.version

  //specific highly used properties
  var TimingEnabled: Boolean = false

  //ahash
  var aHashPrecision = 0
  var aHashTolerance = 0
  var aHashWeight = 0.0f
  var useAhash = false
  //dhash
  var dHashPrecision = 0
  var dHashTolerance = 0
  var dHashWeight = 0.0f
  var useDhash = false
  //phash
  var pHashPrecision = 0
  var pHashTolerance = 0
  var pHashWeight = 0.0f
  var usePhash = false

  //OS information
  val OS = System.getProperty("os.name", "UNKNOWN")
  val OS_VERSION = System.getProperty("os.version", "UNKNOWN")
  val OS_ARCH = System.getProperty("os.arch", "UNKNOWN")

  /*
   * Load the properties file from the specified location
   */
  def loadProperties(defaultLocation: String, userLocation: String = null) = {
    info(s"Attempting to load properties from: $defaultLocation")
    defaultConf = ConfigFactory.load(defaultLocation)
    if (userLocation != null) {
      userConf = ConfigFactory.parseFile(new File(userLocation))
    } else {
      userConf = ConfigFactory.empty
      info("No user properties file exists to load from")
    }
    version = new Version(get(PropertyEnum.Version.toString))
    info(s"Detected Version: $version")

    //load special properties
    TimingEnabled = get(PropertyEnum.Timed.toString).toBoolean

    //ahash
    aHashPrecision = get(PropertyEnum.AhashPrecision.toString).toInt
    aHashTolerance = get(PropertyEnum.AhashTolerance.toString).toInt
    aHashWeight = get(PropertyEnum.AhashWeight.toString).toFloat
    useAhash = get(PropertyEnum.UseAhash.toString).toBoolean
    //dhash
    dHashPrecision = get(PropertyEnum.DhashPrecision.toString).toInt
    dHashTolerance = get(PropertyEnum.DhashTolerance.toString).toInt
    dHashWeight = get(PropertyEnum.DhashWeight.toString).toFloat
    useDhash = get(PropertyEnum.UseDhash.toString).toBoolean
    //phash
    pHashPrecision = get(PropertyEnum.PhashPrecision.toString).toInt
    pHashTolerance = get(PropertyEnum.PhashTolerance.toString).toInt
    pHashWeight = get(PropertyEnum.PhashWeight.toString).toFloat
    usePhash = get(PropertyEnum.UsePhash.toString).toBoolean
    info("Loaded Special Properties")
  }

  private def cleanAndPrepareNewUserProperties(): Properties = {
    //insert special keys here
    newUserConf.setProperty(PropertyEnum.PreviousVersion.toString, version.parsableToString())
    //remove special keys here
    newUserConf.remove(PropertyEnum.Version.toString)
    newUserConf
  }

  private def getCleanedMergedUserConf: Config = {
    ConfigFactory.parseProperties(cleanAndPrepareNewUserProperties()) withFallback userConf
  }

  def saveConf(location: String) = {
    info(s"Saving user properties to $location")
    val out: PrintStream = new PrintStream(new FileOutputStream(location, false))
    val userConfToSave = getCleanedMergedUserConf
    //print to the output stream
    out.print(userConfToSave.root().render(configRenderOptions))
    out.flush()
    out.close()
  }

  def has(key: String): Boolean = {
    var result = false
    if (newUserConf.containsKey(key)
      || userConf.hasPath(key)
      || defaultConf.hasPath(key)) {
      result = true
    }
    result
  }

  def get(key: String, defaultValue: String = null): String = {
    var result: String = defaultValue
    //check the latest properties
    if (newUserConf.containsKey(key)) {
      result = newUserConf.getProperty(key)
    }
    //check the loaded user properties
    else if (userConf.hasPath(key)) {
      result = userConf.getString(key)
    }
    //check the default properties
    else if (defaultConf.hasPath(key)) {
      result = defaultConf.getString(key)
    }
    result
  }

  def set(key: String, value: String) = {
    newUserConf.setProperty(key, value)
  }

}