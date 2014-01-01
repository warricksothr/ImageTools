package com.sothr.imagetools;

import com.sothr.imagetools.errors.ImageToolsException;
import com.sothr.imagetools.ui.controller.AppController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * Hello world!
 *
 */
public class App extends Application
{

    private static Logger logger = LoggerFactory.getLogger(App.class);

    private static final String MAINGUI_FXML = "fxml/mainapp/MainApp.fxml";

    public static void main( String[] args )
    {
        //Logging Config
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
            //try to run the UI
            launch(args);
        } catch (Exception ex) {
            logger.error("A fatal error has occurred: ", ex);
            //show popup about the error to the user then exit
        }

        logger.info("Image-Tools is shutting down");
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        logger.info(String.format("Launching GUI with FXML file %s", MAINGUI_FXML));
        ClassLoader cl = this.getClass().getClassLoader();
        try {
            Parent root = FXMLLoader.load(cl.getResource(MAINGUI_FXML));
            primaryStage.setScene(new Scene(root));
            //config main scene
            primaryStage.setTitle("Image Tools");
            primaryStage.setMinHeight(600.0);
            primaryStage.setMinWidth(800.0);
            primaryStage.setResizable(true);
            //show main scene
            primaryStage.show();
        } catch (IOException ioe) {
            String message = String.format("Unable to load FXML file: %s", MAINGUI_FXML);
            ImageToolsException ite = new ImageToolsException(message, ioe);
            logger.error(message, ioe);
            throw ite;
        } catch (Exception ex) {
            String message = "An unhandled exception was thrown by the GUI";
            ImageToolsException ite = new ImageToolsException(message, ex);
            logger.error(message, ex);
            throw ite;
        }
    }
}
