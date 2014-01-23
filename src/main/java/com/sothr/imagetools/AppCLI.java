package com.sothr.imagetools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CLI interface for Image Tools
 */
class AppCLI {

    private static Logger logger;

    public static void main(String[] args) {
        AppConfig.configureApp();
        logger = LoggerFactory.getLogger(AppCLI.class);
        logger.info("Started Image Tools CLI");
        System.out.println("Hello World");
    }

}
