package com.sothr.imagetools.util

import com.typesafe.config.{Config, ConfigFactory}
import grizzled.slf4j.Logging
import java.io.{File, PrintStream, FileOutputStream}
import java.util.Properties
import scala.collection.JavaConversions._

/*
 * Service for loading and interacting with the properties file
 */
object PropertiesService extends Logging {

  private var defaultConf:Config = null
  private var userConf:Config = null
  private var newUserConf:Properties = new Properties()
  private var version:Version = null
  def getVersion:Version = this.version

  //specific highly used properties
  var TimingEnabled:Boolean = false
  
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

  /*
   * Load the properties file from the specified location
   */
  def loadProperties(defaultLocation:String, userLocation:String = null) = {
    info(s"Attempting to load properties from: $defaultLocation")
    defaultConf = ConfigFactory.load(defaultLocation);
    if (userLocation != null) {
      userConf = ConfigFactory.parseFile(new File(userLocation));
    } else {
      userConf = ConfigFactory.empty
      info("No user properties file exists to load from")
    }
    version = new Version(get(PropertiesEnum.Version.toString));
    info(s"Detected Version: $version")
    
    //load special properties
    TimingEnabled = get(PropertiesEnum.Timed.toString).toBoolean
    
    //ahash
    aHashPrecision = get(PropertiesEnum.AhashPrecision.toString).toInt
    aHashTolerance = get(PropertiesEnum.AhashTolerance.toString).toInt
    aHashWeight = get(PropertiesEnum.AhashWeight.toString).toFloat
    useAhash = get(PropertiesEnum.UseAhash.toString).toBoolean
    //dhash
    dHashPrecision = get(PropertiesEnum.DhashPrecision.toString).toInt
    dHashTolerance = get(PropertiesEnum.DhashTolerance.toString).toInt
    dHashWeight = get(PropertiesEnum.DhashWeight.toString).toFloat
    useDhash = get(PropertiesEnum.UseDhash.toString).toBoolean
    //phash
    pHashPrecision = get(PropertiesEnum.PhashPrecision.toString).toInt
    pHashTolerance = get(PropertiesEnum.PhashTolerance.toString).toInt
    pHashWeight = get(PropertiesEnum.PhashWeight.toString).toFloat
    usePhash = get(PropertiesEnum.UsePhash.toString).toBoolean
    info("Loaded Special Properties")
  }

  private def cleanAndPrepareNewUserProperties():Properties = {
    //insert special keys here
    newUserConf.setProperty(PropertiesEnum.PreviousVersion.toString, version.parsableToString())
    //remove special keys here
    newUserConf.remove(PropertiesEnum.Version.toString)
    newUserConf
  }

  private def getCleanedMergedUserConf():Config = {

      ConfigFactory.parseProperties(cleanAndPrepareNewUserProperties()) withFallback(userConf)
  }

  def saveConf(location:String) = {
    info(s"Saving user properties to $location")
    val out:PrintStream = new PrintStream(new FileOutputStream(location, false))
    val userConfToSave = getCleanedMergedUserConf
    //print to the output stream
    out.print(userConfToSave.root.render)
    out.flush()
    out.close()
  }

  def get(key:String, defaultValue:String=null):String = {
    var result:String = defaultValue
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
    return result
  }

  def set(key:String, value:String) = {
    newUserConf.setProperty(key, value)
  }

}