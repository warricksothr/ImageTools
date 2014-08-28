package com.sothr.imagetools.ui.controller

import java.io.{File, IOException}
import java.util.ArrayList
import java.util.Scanner
import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.{Label, ProgressBar}
import javafx.scene.text.{Text, TextAlignment}
import javafx.scene.web.WebView
import javafx.scene.{Group, Node, Scene}
import javafx.stage.{DirectoryChooser, Stage, StageStyle}

import akka.actor._
import com.sothr.imagetools.engine.image.{SimilarImages, Image}
import com.sothr.imagetools.engine.util.{PropertiesService, ResourceLoader}
import com.sothr.imagetools.engine._
import com.sothr.imagetools.ui.component.ImageTileFactory
import grizzled.slf4j.Logging
import org.markdown4j.Markdown4jProcessor

import scala.concurrent._
import scala.util.{Failure, Success}
import ExecutionContext.Implicits.global

/**
 * Main Application controller
 *
 * Created by drew on 12/31/13.
 */
class AppController extends Logging {

  //Define controls
  @FXML var rootPane: javafx.scene.layout.AnchorPane = null
  @FXML var rootMenuBar: javafx.scene.control.MenuBar = null
  @FXML var imageTilePane: javafx.scene.layout.TilePane = null
  @FXML var tagListView: javafx.scene.control.ListView[String] = null

  // Labels
  @FXML var selectedDirectoryLabel: javafx.scene.control.Label = null
  @FXML var progressLabel: javafx.scene.control.Label = null

  // Others
  @FXML var progressBar: javafx.scene.control.ProgressBar = null

  // Engine
  val engine: Engine = new ConcurrentEngine()

  // Current State
  var currentDirectory: String = "."

  @FXML def initialize() = {
    if (PropertiesService.has("lastPath")) {
      currentDirectory = PropertiesService.get("lastPath", ".")
      selectedDirectoryLabel.setText(PropertiesService.get("lastPath", ""))

      //setup the engine listener
      val system: ActorSystem = AppConfig.getAppActorSystem
      val guiListenerProps: Props = Props.create(classOf[GUIEngineListener])
      val guiListener: ActorRef = system.actorOf(guiListenerProps)
      // configure the listener
      guiListener ! SetupListener(progressBar, progressLabel)
      // tell the engine to use our listener
      this.engine.setProcessedListener(guiListener)
      this.engine.setSimilarityListener(guiListener)
      // Initialize the progress label
      guiListener ! SubmitMessage("Initialized System... Ready!")
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
  def helpAction(event: ActionEvent) = {
    showExternalHTMLUtilityDialog("http://www.sothr.com")
  }

  @FXML
  def aboutAction(event: ActionEvent) = {
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
      case ioe: IOException =>
        error("Unable to read about file")
    }

    showMarkdownUtilityDialog("About", aboutMessage, 400.0, 300.0)
    debug("Showing About Dialog")
  }

  @FXML
  def closeAction(event: ActionEvent) = {
    debug("Closing application from the menu bar")
    val stage: Stage = this.rootMenuBar.getScene.getWindow.asInstanceOf[Stage]
    stage.close()
  }

  //endregion

  //region buttons

  @FXML
  def browseFolders(event: ActionEvent) = {
    val chooser = new DirectoryChooser()
    chooser.setTitle("ImageTools Browser")

    val defaultDirectory = new File(currentDirectory)
    chooser.setInitialDirectory(defaultDirectory)
    val window = this.rootPane.getScene.getWindow
    val selectedDirectory = chooser.showDialog(window)
    info(s"Selected Directory: ${selectedDirectory.getAbsolutePath}")
    selectedDirectoryLabel.setText(selectedDirectory.getAbsolutePath)

    currentDirectory = selectedDirectory.getAbsolutePath
    PropertiesService.set("lastPath", selectedDirectory.getAbsolutePath)
  }

  @FXML
  def showAllImages(event: ActionEvent) = {
    imageTilePane.getChildren.setAll(new ArrayList[Node]())
    val f: Future[List[Image]] = Future {
      engine.getImagesForDirectory(currentDirectory)
    }

    f onComplete {
      case Success(images) =>
        info(s"Displaying ${images.length} images")
        // This is used so that JavaFX updates on the proper thread
        // This is important since UI updates can only happen on that thread
        Platform.runLater(new Runnable() {
          override def run() {
            for (image <- images) {
              debug(s"Adding image ${image.toString} to app")
              imageTilePane.getChildren.add(ImageTileFactory.get(image))
            }
          }
        })
      case Failure(t) =>
        error("An Error Occurred", t)
    }
  }

  @FXML
  def showSimilarImages(event: ActionEvent) = {
    imageTilePane.getChildren.setAll(new ArrayList[Node]())

    val f: Future[List[SimilarImages]] = Future {
      engine.getSimilarImagesForDirectory(currentDirectory)
    }

    f onComplete {
      case Success(similarImages) =>
        info(s"Displaying ${similarImages.length} similar images")
        Platform.runLater(new Runnable() {
          override def run() {
            for (similarImage <- similarImages) {
              debug(s"Adding similar images ${similarImage.rootImage.toString} to app")
              imageTilePane.getChildren.add(ImageTileFactory.get(similarImage.rootImage))
              similarImage.similarImages.foreach(image => imageTilePane.getChildren.add(ImageTileFactory.get(image)))
            }
          }
        })
      case Failure(t) =>
        error("An Error Occurred", t)
    }
  }

  //endregion

  //todo: include a templating engine for rendering information

  //todo: show a dialog that is rendered from markdown content
  def showMarkdownUtilityDialog(title: String, markdown: String, width: Double = 800.0, height: Double = 600.0) = {
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
  def showHTMLUtilityDialog(title: String, htmlBody: String, width: Double = 800.0, height: Double = 600.0) = {
    val dialog: Stage = new Stage()
    dialog.initStyle(StageStyle.UTILITY)
    val parent: Group = new Group()

    //setup the HTML view
    val htmlView = new WebView
    htmlView.getEngine.loadContent(htmlBody)
    htmlView.setMinWidth(width)
    htmlView.setMinHeight(height)
    htmlView.setPrefWidth(width)
    htmlView.setPrefHeight(height)
    parent.getChildren.add(htmlView)

    val scene: Scene = new Scene(parent)
    dialog.setScene(scene)
    dialog.setResizable(false)
    dialog.setTitle(title)
    dialog.show()
  }

  def showExternalHTMLUtilityDialog(url: String) = {
    val dialog: Stage = new Stage()
    dialog.initStyle(StageStyle.UTILITY)
    val parent: Group = new Group()

    //setup the HTML view
    val htmlView = new WebView
    htmlView.getEngine.load(url)
    //htmlView.setMinWidth(width)
    //htmlView.setMinHeight(height)
    //htmlView.setPrefWidth(width)
    //htmlView.setPrefHeight(height)
    parent.getChildren.add(htmlView)

    val scene: Scene = new Scene(parent)
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
  def showUtilityDialog(title: String,
                        message: String,
                        wrapWidth: Double = 300.0,
                        xOffset: Double = 25.0,
                        yOffset: Double = 25.0,
                        alignment: TextAlignment = TextAlignment.JUSTIFY) = {
    val dialog: Stage = new Stage()
    dialog.initStyle(StageStyle.UTILITY)
    val parent: Group = new Group()

    // fill the text box
    val messageText = new Text()
    messageText.setText(message)
    messageText.setWrappingWidth(wrapWidth)
    messageText.setX(xOffset)
    messageText.setY(yOffset)
    messageText.setTextAlignment(TextAlignment.JUSTIFY)

    parent.getChildren.add(messageText)
    val scene: Scene = new Scene(parent)
    dialog.setScene(scene)
    dialog.setResizable(false)
    dialog.setMinWidth(wrapWidth + xOffset * 2)
    dialog.setTitle(title)
    dialog.show()
  }

  def print(): String = {
    "This method works"
  }
}

//region EngineListener
case class SetupListener(progressBar: ProgressBar, progressLabel: Label)

/**
 * Actor for logging output information
 */
class GUIEngineListener extends EngineListener with ActorLogging {
  var progressBar: javafx.scene.control.ProgressBar = null
  var progressLabel: javafx.scene.control.Label = null

  var isStarted = false
  var isFinished = false

  override def receive: Actor.Receive = {
    case command: SetupListener => setupListener(command)
    case command: SubmitMessage => handleMessage(command)
    case command: ScannedFileCount => handleScannedFileCount(command)
    case command: ComparedFileCount => handleComparedFileCount(command)
    case _ => log.info("received unknown message")
  }

  def setupListener(command: SetupListener) = {
    this.progressBar = command.progressBar
    this.progressLabel = command.progressLabel
  }

  override def handleComparedFileCount(command: ComparedFileCount): Unit = {
    Platform.runLater(new Runnable() {
      override def run(): Unit = {
        if (command.message != null) {
          log.debug(command.message)
          progressLabel.setText(command.message)
        } else {
          progressLabel.setText(s"Processed ${command.count}/${command.total}")
        }
        log.debug("Processed {}/{}", command.count, command.total)
        progressBar.setProgress(command.count.toFloat/command.total)
      }
    })
  }

  override def handleScannedFileCount(command: ScannedFileCount): Unit = {
    Platform.runLater(new Runnable() {
      override def run(): Unit = {
        if (command.message != null) {
          log.debug(command.message)
          progressLabel.setText(command.message)
        } else {
          progressLabel.setText(s"Scanned ${command.count}/${command.total} For Similarities")
        }
        log.debug("Scanned {}/{} For Similarities", command.count, command.total)
        progressBar.setProgress(command.count.toFloat/command.total)
      }
    })
  }

  override def handleMessage(command: SubmitMessage): Unit = {
    Platform.runLater(new Runnable() {
      override def run(): Unit = {
        log.debug(command.message)
        progressLabel.setText(command.message)
      }
    })
  }
}

//endregion
