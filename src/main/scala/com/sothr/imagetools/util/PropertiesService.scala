package com.sothr.imagetools.util

import java.util.Properties
import grizzled.slf4j.Logging
import java.io.{FileInputStream, FileOutputStream, OutputStream}
import scala.collection.JavaConversions._

/*
 * Service for loading and interacting with the properties file
 */
object PropertiesService extends Logging {

  private val properties:Properties = new Properties()
  private var version:Version = null
  private val propertiesToClean:Array[String] = Array("version")

  /*
   * Load the properties file from the specified location
   */
  def loadProperties(defaultLocation:String, userLocation:String = null) = {
    info(s"Attempting to load properties from: $defaultLocation")
    val defaultInputStream = ResourceLoader.get.getResourceStream(defaultLocation)
    properties.load(defaultInputStream)
    if (userLocation != null) {
      val userInputStream = new FileInputStream(userLocation)
      val userProperties = new Properties();
      userProperties.loadFromXML(userInputStream)

      for (propertyName:String <- userProperties.stringPropertyNames()) {
        properties.setProperty(propertyName, userProperties.getProperty(propertyName));
      }
    } else {
      info("No user properties file exists to load from")
    }
    version = new Version(properties.getProperty("version"));
    info(s"Detected Version: $version")
  }

  /**
   * Gets a properties object that is cleaned of things that are expected to change. i.e. version
   */
  private def getCleanProperties():Properties = {
    val cleanProperties:Properties = properties.clone().asInstanceOf[Properties]
    //Remove properties to be cleaned
    for (key <- propertiesToClean) {
      cleanProperties.remove(key)
    }
    return cleanProperties
  }

  def saveXMLProperties(location:String) = {
    info(s"Saving user properties to $location")
    val out:OutputStream = new FileOutputStream(location, false)
    val cleanProperties = getCleanProperties
    //insert special keys here
    cleanProperties.setProperty("version.previous", version.toString())
    cleanProperties.storeToXML(out, "User Properties")
    out.flush()
    out.close()
  }

  def get(key:String):String = {
    return properties.getProperty(key)
  }

  def set(key:String, value:String) = {
    properties.setProperty(key, value)
  }

}