package com.sothr.imagetools.engine;

import akka.actor.ActorSystem;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import com.sothr.imagetools.engine.dao.HibernateUtil;
import com.sothr.imagetools.engine.util.PropertiesService;
import com.sothr.imagetools.engine.util.ResourceLoader;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import net.sf.ehcache.CacheManager;
import org.slf4j.LoggerFactory;

import java.io.File;

public class AppConfig {

  // Logging defaults
  private static final String LOGSETTINGSFILE = "./logback.xml";
  // Properties defaults
  private static final String DEFAULTPROPERTIESFILE = "application.conf";
  private static final String USERPROPERTIESFILE = "user.conf";
  // General Akka Actor System
  private static final ActorSystem appSystem = ActorSystem.create("ITActorSystem");
  public static CacheManager cacheManager;
  public static FXMLLoader fxmlLoader = null;
  private static Logger logger;
  private static Boolean configuredLogging = false;
  private static Boolean loadedProperties = false;
  // Cache defaults
  private static Boolean configuredCache = false;
  // The Main App
  private static Stage primaryStage = null;

  public static Stage getPrimaryStage() {
    return primaryStage;
  }

  public static void setPrimaryStage(Stage newPrimaryStage) {
    primaryStage = newPrimaryStage;
  }

  public static FXMLLoader getFxmlLoader() {
    return fxmlLoader;
  }

  public static void setFxmlLoader(FXMLLoader loader) {
    fxmlLoader = loader;
  }

  public static ActorSystem getAppActorSystem() {
    return appSystem;
  }

  public static void configureApp() {
    logger = (Logger) LoggerFactory.getLogger(AppConfig.class);
    loadProperties();
    configLogging();
    configCache();
  }

  private static void configLogging(String location) {
    //Logging Config
    //remove previous configuration if it exists
    Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
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
        configurator.doConfigure(ResourceLoader.get().getResourceStream("logback-minimum-config.xml"));
      } catch (JoranException je) {
        // StatusPrinter will handle this
      }
      StatusPrinter.printInCaseOfErrorsOrWarnings(context);
    }
    String message = fromFile ? "From File" : "From Defaults";
    logger.info(String.format("Configured Logger %s", message));
    logger.info(String.format("Detected Version: %s of Image Tools", PropertiesService.getVersion().toString()));
    logger.info(String.format("Running on %s, %s, %s", PropertiesService.OS(), PropertiesService.OS_VERSION(), PropertiesService.OS_ARCH()));
  }

  //Only configure logging from the default file once
  private static void configLogging() {
    if (!configuredLogging) {
      configLogging(LOGSETTINGSFILE);
      configuredLogging = true;
      logger.info("Configured logging");
    }
  }

  private static void loadProperties() {
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

  private static void configCache() {
    if (!configuredCache) {
      cacheManager = CacheManager.newInstance();
      configuredCache = true;
      logger.info("Configured EHCache");
    }
  }

  public static void shutdown() {
    saveProperties();
    HibernateUtil.getSessionFactory().close();
  }

  private static void saveProperties() {
    PropertiesService.saveConf(USERPROPERTIESFILE);
    logger.debug("Saved properties");
  }

}
