package com.sothr.imagetools;

import com.sothr.imagetools.util.PropertiesService;
import com.sothr.imagetools.util.PropertiesEnum;
import net.sf.ehcache.CacheManager;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Properties;

public class AppConfig {

  private static Logger logger;
  public static CacheManager cacheManager;

  //Logging defaults
  private static final String LOGSETTINGSFILE = "./log4j.properties";
  private static Boolean configuredLogging = false;

  //Properties defaults
  private static final String DEFAULTPROPERTIESFILE = "default.properties";
  private static final String USERPROPERTIESFILE = "./config.xml";
  private static Boolean loadedProperties = false;

  //Cache defaults
  private static Boolean configuredCache = false;

  public static void configureApp() {
    //configSimpleLogging();
    if (!configuredLogging) {
      configBasicLogging();
      loadProperties();
      resetBasicLogging();
    } else {
      loadProperties();
    }
    configLogging();
    configCache();
  }

  public static void configBasicLogging() {
    BasicConfigurator.configure();
    logger = LoggerFactory.getLogger(AppConfig.class);
  }

  public static void resetBasicLogging() {
    logger = null;
    BasicConfigurator.resetConfiguration();
  }

  public static void configLogging(String location) {
    //Logging Config
    //remove previous configuration if it exists
    //BasicConfigurator.resetConfiguration();
    File file = new File(location);
    Boolean fromFile = false;
    if (file.exists()) {
      fromFile = true;
      PropertyConfigurator.configure(location);
    } else {
      //Simple error logging configuration
      Properties defaultProps = new Properties();
      String rootLogger = "DEBUG";
      if (Boolean.valueOf(PropertiesService.get(PropertiesEnum.LogDebug().toString()))) {
        //Rolling Debug logger
        rootLogger += ", DL";
        defaultProps.setProperty("log4j.appender.DL","org.apache.log4j.RollingFileAppender");
        defaultProps.setProperty("log4j.appender.DL.Threshold","DEBUG");
        defaultProps.setProperty("log4j.appender.DL.File","Image-Tools.debug");
        defaultProps.setProperty("log4j.appender.DL.MaxFileSize","500KB");
        defaultProps.setProperty("log4j.appender.DL.MaxBackupIndex","1");
        defaultProps.setProperty("log4j.appender.DL.layout","org.apache.log4j.EnhancedPatternLayout");
        defaultProps.setProperty("log4j.appender.DL.layout.ConversionPattern","%d{yy-MM-dd HH:mm:ss} %-5p [%c{3.}] - %m%n");
      }
      if (Boolean.valueOf(PropertiesService.get(PropertiesEnum.LogInfo().toString()))) {
        //Rolling Info logger
        rootLogger += ", IL";
        defaultProps.setProperty("log4j.appender.IL","org.apache.log4j.RollingFileAppender");
        defaultProps.setProperty("log4j.appender.IL.Threshold","INFO");
        defaultProps.setProperty("log4j.appender.IL.File","Image-Tools.info");
        defaultProps.setProperty("log4j.appender.IL.MaxFileSize","100KB");
        defaultProps.setProperty("log4j.appender.IL.MaxBackupIndex","1");
        defaultProps.setProperty("log4j.appender.IL.layout","org.apache.log4j.EnhancedPatternLayout");
        defaultProps.setProperty("log4j.appender.IL.layout.ConversionPattern","%d{yy-MM-dd HH:mm:ss} %-5p [%c{3.}] - %m%n");
      }
      if (Boolean.valueOf(PropertiesService.get(PropertiesEnum.LogError().toString()))) {
        //Rolling Error logger
        rootLogger += ", EL";
        defaultProps.setProperty("log4j.appender.EL","org.apache.log4j.RollingFileAppender");
        defaultProps.setProperty("log4j.appender.EL.Threshold","ERROR");
        defaultProps.setProperty("log4j.appender.EL.File","Image-Tools.err");
        defaultProps.setProperty("log4j.appender.EL.MaxFileSize","100KB");
        defaultProps.setProperty("log4j.appender.EL.MaxBackupIndex","1");
        defaultProps.setProperty("log4j.appender.EL.layout","org.apache.log4j.EnhancedPatternLayout");
        defaultProps.setProperty("log4j.appender.EL.layout.ConversionPattern","%d{yy-MM-dd HH:mm:ss} %-5p [%c{3.}] - %m%n");
      }
      defaultProps.setProperty("log4j.rootLogger",rootLogger);
      PropertyConfigurator.configure(defaultProps);
    }
    logger = LoggerFactory.getLogger(AppConfig.class);
    String message = fromFile ? "From File" : "From Defaults";
    logger.info(String.format("Configured Logger %s", message));
    logger.info("Detected Version: %s of Image Tools".format(PropertiesService.getVersion().toString()));
  }

  //Only configure logging from the default file once
  public static void configLogging() {
    if (!configuredLogging) {
      configLogging(LOGSETTINGSFILE);
      configuredLogging = true;
      logger.info("Configured logging");
    }
  }

  public static void loadProperties() {
    if (!loadedProperties) {
      File file = new File(USERPROPERTIESFILE);
      if (file.exists()) {
        PropertiesService.loadProperties(DEFAULTPROPERTIESFILE, USERPROPERTIESFILE);
      } else {
        PropertiesService.loadProperties(DEFAULTPROPERTIESFILE, null);
      }
      loadedProperties = true;
      logger.info("Loaded Properties");
    }
  }

  public static void configCache() {
    if (!configuredCache) {
      cacheManager = CacheManager.newInstance();
      configuredCache = true;
      logger.info("Configured EHCache");
    }
  }

  public static void saveProperties() {
    PropertiesService.saveXMLProperties(USERPROPERTIESFILE);
    logger.debug("Saved properties");
  }

}
