package com.sothr.imagetools.ui;

import com.sothr.imagetools.engine.AppConfig;
import com.sothr.imagetools.engine.errors.ImageToolsException;
import com.sothr.imagetools.engine.util.ResourceLoader;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Image Tools
 */
public class App extends Application {

  private static Logger logger;

  private static final String MAINGUI_FXML = "fxml/mainapp/MainApp.fxml";

  public static void main(String[] args) {
    AppConfig.configureApp();

    try {
      //try to run the UI
      launch(args);
    } catch (Exception ex) {
      logger.error("A fatal error has occurred: ", ex);
      //show popup about the error to the user then exit
    }
  }

  @Override
  public void init() throws Exception {
    AppConfig.configureApp();
    logger = LoggerFactory.getLogger(this.getClass());
    logger.info("Initializing Image Tools");
    List<String> parameters = this.getParameters().getRaw();
    logger.info(String.format("Application was called with '%s' parameters", parameters.toString()));
    super.init();
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    logger.info("Image-Tools is starting");
    logger.info(String.format("Launching GUI with FXML file %s", MAINGUI_FXML));
    //store the primary stage globally for reference in popups and the like
    AppConfig.setPrimaryStage(primaryStage);
    try {
      Parent root = FXMLLoader.load(ResourceLoader.get().getResource(MAINGUI_FXML));
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

  @Override
  public void stop() throws Exception {
    logger.info("Image-Tools is shutting down");
    AppConfig.shutdown();
    super.stop();
    //force the JVM to close
    System.exit(0);
  }
}
