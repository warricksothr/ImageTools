package com.sothr.imagetools.ui.controller

import java.io.{File, IOException}
import java.util
import java.util.Scanner
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.text.{Text, TextAlignment}
import javafx.scene.web.WebView
import javafx.scene.{Group, Node, Scene}
import javafx.stage.{DirectoryChooser, Stage, StageStyle}

import com.sothr.imagetools.engine.{Engine, SequentialEngine}
import com.sothr.imagetools.ui.component.ImageTileFactory
import com.sothr.imagetools.util.{PropertiesService, ResourceLoader}
import grizzled.slf4j.Logging
import org.markdown4j.Markdown4jProcessor

/**
 * Main Application controller
 *
 * Created by drew on 12/31/13.
 */
class AppController extends Logging {

  //Define controls
  @FXML var rootPane : javafx.scene.layout.AnchorPane = null
  @FXML var rootMenuBar : javafx.scene.control.MenuBar = null
  @FXML var imageTilePane : javafx.scene.layout.TilePane = null
  @FXML var tagListView : javafx.scene.control.ListView[String] = null

  // Labels
  @FXML var selectedDirectoryLabel: javafx.scene.control.Label = null

  // Engine
  val engine:Engine = new SequentialEngine()

  // Current State
  var currentDirectory:String = "."

  @FXML def initialize() = {
    if (PropertiesService.has("lastPath")) {
      currentDirectory = PropertiesService.get("lastPath", ".")
      selectedDirectoryLabel.setText(PropertiesService.get("lastPath", ""))
    }

    //test
    //val testImage = new Image()
    //testImage.setThumbnailPath("test.jpg")
    //testImage.setImagePath("test.jpg")
    //for (i <- 1 to 100) {
    //  imageTilePane.getChildren.add(ImageTileFactory.get(testImage))
    //}
    //val list = FXCollections.observableArrayList[String]()
    //for (i <- 1 to 100) {
    //  list.add(s"test-item ${i}")
    //}
    //tagListView.setItems(list)
  }

  //region MenuItem Actions

  @FXML
  def helpAction(event:ActionEvent) = {
    showExternalHTMLUtilityDialog("http://www.sothr.com")
  }

  @FXML
  def aboutAction(event:ActionEvent) = {
    debug("Displaying about screen")
    var aboutMessage = "Simple About Message"
    try {

      val scanner = new Scanner(ResourceLoader.get().getResourceStream("documents/about.md"))
      aboutMessage = ""
      while (scanner.hasNextLine) {
        aboutMessage += scanner.nextLine().trim() + "\n"
      }

      debug(s"Parsed About Message: '$aboutMessage'")

    } catch {
      case ioe:IOException =>
        error("Unable to read about file")
    }

    showMarkdownUtilityDialog("About", aboutMessage, 400.0, 300.0)
    debug("Showing About Dialog")
  }

  @FXML
  def closeAction(event:ActionEvent) = {
    debug("Closing application from the menu bar")
    val stage:Stage = this.rootMenuBar.getScene.getWindow.asInstanceOf[Stage]
    stage.close()
  }

  @FXML
  def browseFolders(event:ActionEvent) = {
    val chooser = new DirectoryChooser()
    chooser.setTitle("ImageTools Browser")

    val defaultDirectory = new File(currentDirectory)
    chooser.setInitialDirectory(defaultDirectory)
    val window = this.rootPane.getScene.getWindow
    val selectedDirectory = chooser.showDialog(window)
    info(s"Selected Directory: ${selectedDirectory.getAbsolutePath}")
    selectedDirectoryLabel.setText(selectedDirectory.getAbsolutePath)

    currentDirectory = selectedDirectory.getAbsolutePath
    PropertiesService.set("lastPath",selectedDirectory.getAbsolutePath)
  }

  @FXML
  def showAllImages(event:ActionEvent) = {
    imageTilePane.getChildren.setAll(new util.ArrayList[Node]())
    val images = engine.getImagesForDirectory(currentDirectory)
    info(s"Displaying ${images.length} images")
    for (image <- images) {
      debug(s"Adding image ${image.toString} to app")
      imageTilePane.getChildren.add(ImageTileFactory.get(image))
    }
  }

  @FXML
  def showSimilarImages(event:ActionEvent) = {
    imageTilePane.getChildren.setAll(new util.ArrayList[Node]())
    val similarImages = engine.getSimilarImagesForDirectory(currentDirectory)
    info(s"Displaying ${similarImages.length} similar images")
    for (similarImage <- similarImages) {
      debug(s"Adding similar images ${similarImage.rootImage.toString} to app")
      imageTilePane.getChildren.add(ImageTileFactory.get(similarImage.rootImage))
      similarImage.similarImages.foreach( image => imageTilePane.getChildren.add(ImageTileFactory.get(image)))
    }
  }

  //endregion

  //todo: include a templating engine for rendering information

  //todo: show a dialog that is rendered from markdown content
  def showMarkdownUtilityDialog(title:String, markdown:String, width:Double = 800.0, height:Double = 600.0) = {
    val htmlBody = new Markdown4jProcessor().process(markdown)
    showHTMLUtilityDialog(title, htmlBody, width, height)
  }

  /**
   * Render HTML content to a utility dialog. No input or output, just raw rendered content through a webkit engine.
   *
   * @param title Title of the dialog
   * @param htmlBody Body to render
   * @param width Desired width of the dialog
   * @param height Desired height of the dialog
   */
  def showHTMLUtilityDialog(title:String, htmlBody:String, width:Double = 800.0, height:Double = 600.0) = {
    val dialog:Stage = new Stage()
    dialog.initStyle(StageStyle.UTILITY)
    val parent:Group = new Group()

    //setup the HTML view
    val htmlView = new WebView
    htmlView.getEngine.loadContent(htmlBody)
    htmlView.setMinWidth(width)
    htmlView.setMinHeight(height)
    htmlView.setPrefWidth(width)
    htmlView.setPrefHeight(height)
    parent.getChildren.add(htmlView)

    val scene:Scene = new Scene(parent)
    dialog.setScene(scene)
    dialog.setResizable(false)
    dialog.setTitle(title)
    dialog.show()
  }

  def showExternalHTMLUtilityDialog(url:String) = {
    val dialog:Stage = new Stage()
    dialog.initStyle(StageStyle.UTILITY)
    val parent:Group = new Group()

    //setup the HTML view
    val htmlView = new WebView
    htmlView.getEngine.load(url)
    //htmlView.setMinWidth(width)
    //htmlView.setMinHeight(height)
    //htmlView.setPrefWidth(width)
    //htmlView.setPrefHeight(height)
    parent.getChildren.add(htmlView)

    val scene:Scene = new Scene(parent)
    dialog.setScene(scene)
    dialog.setResizable(false)
    dialog.setTitle(htmlView.getEngine.getTitle)
    dialog.show()
  }

  /**
   * Show a plain text utility dialog
   *
   * @param message Message to display
   * @param wrapWidth When to wrap
   * @param alignment How it should be aligned
   */
  def showUtilityDialog(title:String,
                        message:String,
                        wrapWidth:Double=300.0,
                        xOffset:Double = 25.0,
                        yOffset:Double = 25.0,
                        alignment:TextAlignment=TextAlignment.JUSTIFY) = {
    val dialog:Stage = new Stage()
    dialog.initStyle(StageStyle.UTILITY)
    val parent:Group = new Group()

    // fill the text box
    val messageText = new Text()
    messageText.setText(message)
    messageText.setWrappingWidth(wrapWidth)
    messageText.setX(xOffset)
    messageText.setY(yOffset)
    messageText.setTextAlignment(TextAlignment.JUSTIFY)

    parent.getChildren.add(messageText)
    val scene:Scene = new Scene(parent)
    dialog.setScene(scene)
    dialog.setResizable(false)
    dialog.setMinWidth(wrapWidth+xOffset*2)
    dialog.setTitle(title)
    dialog.show()
  }

  def print():String = {
    "This method works"
  }
}
