package com.sothr.imagetools;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Properties;

/**
 * Hello world!
 *
 */
public class App 
{

    private static Logger logger = LoggerFactory.getLogger(App.class);

    public static void main( String[] args )
    {
        File file = new File("log4j.properties");
        if (file.exists()) {
            PropertyConfigurator.configure("log4j.properties");
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

        logger.info("Image-Tools is starting");

        try {
            System.out.println( "Hello World!" );
        } catch (Exception ex) {
            logger.error("A fatal error has occurred: ", ex);
            //show popup about the error to the user then exit
        }

        logger.info("Image-Tools is shutting down");
    }
}
