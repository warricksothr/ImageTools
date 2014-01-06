package com.sothr.imagetools.util

import java.util.Properties
import grizzled.slf4j.Logging

/*
 * Service for loading and interacting with the properties file
 */
object PropertiesService extends Logging {

  val properties:Properties = new Properties()

  /*
   * Load the properties file from the specified location
   */
  def loadProperties(location:String) = {
    info(s"Attempting to load properties from: $location")
    val inputStream = ResourceLoader.get.getResourceStream(location)
    val splitLocation = location.split("""\.""")
    if (splitLocation(splitLocation.length) equals "xml") {
      properties.loadFromXML(inputStream)
    } else if (splitLocation(splitLocation.length) equals "properties") { 
      properties.load(inputStream);
    } else {
      error("Unable to load the properties file because it is not in the .properties or .xml format")
    }
  }

  def saveProperties(location:String) = {
    
  }

}
