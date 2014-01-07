package com.sothr.imagetools;

import com.sothr.imagetools.util.PropertiesService;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Properties;

class AppConfig {

    private static Logger logger;

    //Logging defaults
    private static final String LOGSETTINGSFILE = "./log4j.properties";
    private static Boolean configuredLogging = false;

    //Properties defaults
    private static final String DEFAULTPROPERTIESFILE = "default.properties";
    private static final String USERPROPERTIESFILE = "./config.xml";
    private static Boolean loadedProperties = false;

    public static void configLogging(String location) {
        //Logging Config
        File file = new File(location);
        Boolean fromFile = false;
        if (file.exists()) {
            fromFile = true;
            PropertyConfigurator.configure(location);
        } else {
            //Simple error logging configuration
            Properties defaultProps = new Properties();
            defaultProps.setProperty("log4j.rootLogger","ERROR, A1");
            //Rolling Error logger
            defaultProps.setProperty("log4j.appender.A1","org.apache.log4j.RollingFileAppender");
            defaultProps.setProperty("log4j.appender.A1.File","Image-Tools.err");
            defaultProps.setProperty("log4j.appender.A1.MaxFileSize","100KB");
            defaultProps.setProperty("log4j.appender.A1.MaxBackupIndex","1");
            defaultProps.setProperty("log4j.appender.A1.layout","org.apache.log4j.EnhancedPatternLayout");
            defaultProps.setProperty("log4j.appender.A1.layout.ConversionPattern","%d{yy-MM-dd HH:mm:ss} %-5p [%c{3.}] - %m%n");
            PropertyConfigurator.configure(defaultProps);
        }
        logger = LoggerFactory.getLogger(AppConfig.class);
        String message = fromFile ? "From File" : "From Defaults";
        logger.info(String.format("Configured Logger %s", message));
    }

    //Only configure logging from the default file once
    public static void configLogging() {
        if (!configuredLogging) {
            configLogging(LOGSETTINGSFILE);
            configuredLogging = true;
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
        }
    }

    public static void saveProperties() {
        PropertiesService.saveXMLProperties(USERPROPERTIESFILE);
    }

}
