package com.sothr.imagetools.ui.controller

import javafx.fxml.FXML
import javafx.event.ActionEvent
import org.slf4j.LoggerFactory
import org.slf4j.Logger
import javafx.stage.{StageStyle, Stage}
import javafx.scene.Scene
import javafx.scene.Group
import javafx.scene.text.Text
import java.io.{IOException, File}
import java.util.Scanner
import com.sothr.imagetools.util.ResourceLoader
import java.net.URL

/**
 * Created by drew on 12/31/13.
 */
class AppController {

  val logger:Logger = LoggerFactory.getLogger(this.getClass)

  //Define controls
  @FXML var rootMenuBar : javafx.scene.control.MenuBar = null

  //region MenuItem Actions

  @FXML
  def aboutAction(event:ActionEvent) = {
    logger.debug("Displaying about screen")
    var aboutMessage = "Simple About Message"
    try {
      aboutMessage = new Scanner(ResourceLoader.get().getResourceStream("documents/about")).useDelimiter("\\A").next()
    } catch {
      case ioe:IOException =>
        logger.error("Unable to read about file")
    }

    val dialog:Stage = new Stage()
    dialog.initStyle(StageStyle.UTILITY)
    val parent:Group = new Group();
    parent.getChildren.add(new Text(25, 25, aboutMessage))
    val scene:Scene = new Scene(parent)
    dialog.setScene(scene)
    dialog.setResizable(false)
    dialog.setMinHeight(400.0)
    dialog.setMinWidth(400.0)
    dialog.show()
  }

  @FXML
  def closeAction(event:ActionEvent ) = {
    logger.debug("Closing application from the menu bar")
    val stage:Stage = this.rootMenuBar.getScene.getWindow.asInstanceOf[Stage]
    stage.close()
  }

  //endregion

  def print():String = {
    return "This method works"
  }
}
