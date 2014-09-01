package com.sothr.imagetools.ui.controller

import java.io.{File, IOException}
import java.util.Scanner
import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control._
import javafx.scene.layout.{AnchorPane, TilePane, VBox}
import javafx.scene.text.{Text, TextAlignment}
import javafx.scene.web.WebView
import javafx.scene.{Group, Node, Scene}
import javafx.stage.{DirectoryChooser, Stage, StageStyle}
import javafx.util.Callback

import akka.actor._
import com.sothr.imagetools.engine._
import com.sothr.imagetools.engine.image.{Image, SimilarImages}
import com.sothr.imagetools.engine.util.{PropertiesService, ResourceLoader}
import com.sothr.imagetools.ui.component.{ImageTilePane, ImageTileFactory}
import grizzled.slf4j.Logging
import org.markdown4j.Markdown4jProcessor

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.util.{Failure, Success}

/**
 * Main Application controller
 *
 * Created by drew on 12/31/13.
 */
class AppController extends Logging {

  //Define controls
  @FXML var rootPane: AnchorPane = null
  @FXML var rootMenuBar: MenuBar = null
  @FXML var scrollPane: ScrollPane = null
  @FXML var imageTilePane: TilePane = null
  @FXML var tagListView: ListView[String] = null

  // Labels
  @FXML var selectedDirectoryLabel: Label = null
  @FXML var currentDirectoryLabel: Label = null
  @FXML var progressLabel: Label = null

  // Others
  @FXML var progressBar: ProgressBar = null
  @FXML var paginator: Pagination = null
  @FXML var doRecursiveProcessing: CheckBox = null

  // Engine
  val engine: Engine = new ConcurrentEngine()

  // Current State
  var currentDirectory: String = "."
  var currentImages: List[Image] = List[Image]()

  @FXML def initialize() = {
    if (PropertiesService.has("app.ui.lastPath")) {
      currentDirectory = PropertiesService.get("app.ui.lastPath", ".")
      selectedDirectoryLabel.setText(PropertiesService.get("app.ui.lastPath", ""))
    }

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

    // set the default images per page if it doesn't exist yet
    if (!PropertiesService.has("app.ui.thumbsPerPage")) {
      PropertiesService.set("app.ui.thumbsPerPage", "100")
    }

    //setup the paginator
    //font size doesn't increase the size of the buttons
    //paginator.setStyle("-fx-font-size:13;")
    // configure the page factory
    paginator.setPageFactory(new Callback[Integer, Node]() {
      override def call(pageIndex: Integer): Node = {
        // do all of our display logic
        showPage(pageIndex)
        // override behavior to display anything
        new VBox()
      }
    })

    //override the imageTilePane
    debug("Replacing the default TilePane with a custom ImageTilePane")
    val newImageTilePane = new ImageTilePane()
    newImageTilePane.setHgap(this.imageTilePane.getHgap)
    newImageTilePane.setVgap(this.imageTilePane.getVgap)
    newImageTilePane.setMinHeight(this.imageTilePane.getMinHeight)
    newImageTilePane.setMinWidth(this.imageTilePane.getMinWidth)
    newImageTilePane.setMaxHeight(this.imageTilePane.getMaxHeight)
    newImageTilePane.setMaxWidth(this.imageTilePane.getMaxWidth)
    newImageTilePane.setPrefColumns(this.imageTilePane.getPrefColumns)
    newImageTilePane.setPrefRows(this.imageTilePane.getPrefRows)
    //newImageTilePane.setPrefTileHeight(this.imageTilePane.getPrefTileHeight)
    //newImageTilePane.setPrefTileWidth(this.imageTilePane.getPrefTileWidth)
    newImageTilePane.setTileAlignment(this.imageTilePane.getTileAlignment)
    debug("Assigning the the new ImageTilePane to the ScrollPane")
    this.scrollPane.setContent(newImageTilePane)
    this.imageTilePane = newImageTilePane

    //test
    //val testImage = new Image()
    //testImage.setThumbnailPath("test.jpg")
    //testImage.setImagePath("test.jpg")
    //val testImageList = new mutable.MutableList[Image]
    //for (i <- 1 to 100) {
    //  testImageList += testImage
    //}
    //setPagesContent(testImageList.toList)
    //showPage(0)
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
    PropertiesService.set("app.ui.lastPath", selectedDirectory.getAbsolutePath)
    this.currentDirectoryLabel.setText(selectedDirectory.getAbsolutePath)
  }

  @FXML
  def showAllImages(event: ActionEvent) = {
    resetPaginator()
    getImageTilePane.getChildren.setAll(new java.util.ArrayList[Node]())
    val f: Future[List[Image]] = Future {
      val images = engine.getImagesForDirectory(currentDirectory, recursive = doRecursiveProcessing.isSelected)
      images.sortWith((x,y) => x.imagePath < y.imagePath)
    }

    f onComplete {
      case Success(images) =>
        info(s"Displaying ${images.length} images")
        // This is used so that JavaFX updates on the proper thread
        // This is important since UI updates can only happen on that thread
        Platform.runLater(new Runnable() {
          override def run() {
            setPagesContent(images)
            showPage(0)
          }
        })
      case Failure(t) =>
        error("An Error Occurred", t)
    }
  }

  @FXML
  def showSimilarImages(event: ActionEvent) = {
    resetPaginator()
    imageTilePane.getChildren.setAll(new java.util.ArrayList[Node]())

    val f: Future[List[Image]] = Future {
      val similarImages = engine.getSimilarImagesForDirectory(currentDirectory, recursive = doRecursiveProcessing.isSelected)
      val tempImages = new mutable.MutableList[Image]()
      for (similarImage <- similarImages) {
        debug(s"Adding similar images ${similarImage.rootImage.toString} to app")
        tempImages += similarImage.rootImage
        similarImage.similarImages.foreach(image => tempImages += image)
      }
      tempImages.toList.sortWith((x,y) => x.imagePath < y.imagePath)
    }

    f onComplete {
      case Success(images) =>
        info(s"Displaying ${images.length} similar images")
        Platform.runLater(new Runnable() {
          override def run() {
            setPagesContent(images)
            showPage(0)
          }
        })
      case Failure(t) =>
        error("An Error Occurred", t)
    }
  }

  //endregion

  //region pagination

  def resetPaginator() = {
    this.paginator.setDisable(true)
    this.paginator.setPageCount(1)
  }

  def setPagesContent(images: List[Image]) = {
    this.currentImages = images
    //set the appropriate size for the pagination
    val itemsPerPage = PropertiesService.get("app.ui.thumbsPerPage", "100").toInt
    val pageNum = Math.ceil(this.currentImages.size.toFloat / itemsPerPage).toInt
    this.paginator.setPageCount(pageNum)
    this.paginator.setDisable(false)
  }

  def showPage(pageIndex: Integer) = {
    val itemsPerPage = PropertiesService.get("app.ui.thumbsPerPage", "100").toInt
    val startIndex = pageIndex * itemsPerPage
    val endIndex = if ((startIndex + itemsPerPage) > this.currentImages.size) this.currentImages.length else startIndex + itemsPerPage
    //clear any selections
    getImageTilePane.asInstanceOf[ImageTilePane].clearSelection()
    //clear and populate the scrollpane
    getImageTilePane.getChildren.setAll(new java.util.ArrayList[Node]())
    val images = this.currentImages.slice(startIndex, endIndex)
    Platform.runLater(new Runnable() {
      override def run() {
        for (image <- images) {
          debug(s"Adding image ${image.toString} to app")
          getImageTilePane.getChildren.add(ImageTileFactory.get(image, getImageTilePane))
        }
      }
    })
  }

  //endregion

  def getImageTilePane :TilePane = {
    this.imageTilePane
  }

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
