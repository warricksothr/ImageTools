package com.sothr.imagetools;

import com.sothr.imagetools.util.ResourceLoader;
import com.sothr.imagetools.util.PropertiesService;
import com.sothr.imagetools.util.PropertiesEnum;
import net.sf.ehcache.CacheManager;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

import java.io.File;
import java.util.Properties;

public class AppConfig {

  private static Logger logger;
  public static CacheManager cacheManager;

  //Logging defaults
  private static final String LOGSETTINGSFILE = "./logback.xml";
  private static Boolean configuredLogging = false;

  //Properties defaults
  private static final String DEFAULTPROPERTIESFILE = "default.properties";
  private static final String USERPROPERTIESFILE = "./config.xml";
  private static Boolean loadedProperties = false;

  //Cache defaults
  private static Boolean configuredCache = false;

  public static void configureApp() {
    logger = (Logger)LoggerFactory.getLogger(AppConfig.class);
    loadProperties();
    configLogging();
    configCache();
  }

  public static void configLogging(String location) {
    //Logging Config
    //remove previous configuration if it exists
    Logger rootLogger = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    LoggerContext context = rootLogger.getLoggerContext();
    context.reset();
    File file = new File(location);
    Boolean fromFile = false;
    if (file.exists()) {
        fromFile = true;
        try {
          JoranConfigurator configurator = new JoranConfigurator();
          configurator.setContext(context);
          // Call context.reset() to clear any previous configuration, e.g. default 
          // configuration. For multi-step configuration, omit calling context.reset().
          context.reset(); 
          configurator.doConfigure(location);
        } catch (JoranException je) {
          // StatusPrinter will handle this
        }
        StatusPrinter.printInCaseOfErrorsOrWarnings(context);
    } else {
        try {
          JoranConfigurator configurator = new JoranConfigurator();
          configurator.setContext(context);
          // Call context.reset() to clear any previous configuration, e.g. default 
          // configuration. For multi-step configuration, omit calling context.reset().
          context.reset();
          configurator.doConfigure(ResourceLoader.get().getResource("logback-minimum-config.xml"));
        } catch (JoranException je) {
          // StatusPrinter will handle this
        }
        StatusPrinter.printInCaseOfErrorsOrWarnings(context);
    }
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
