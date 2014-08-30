package com.sothr.imagetools.ui.component

import java.io.FileInputStream
import javafx.event.{EventType, EventHandler}
import javafx.geometry.{Orientation, Insets, Pos}
import javafx.scene.control.{Separator, Tooltip, Label}
import javafx.scene.image.{ImageView}
import javafx.scene.input.{PickResult, ContextMenuEvent, MouseEvent}
import javafx.scene.layout.VBox

import grizzled.slf4j.Logging
import resource._

/**
 * ImageTile class that is a special VBox
 *
 * Created by drew on 8/22/14.
 */
class ImageTile(thumbnailWidth: Integer,
                image: com.sothr.imagetools.engine.image.Image,
                imageTilePane: ImageTilePane) extends VBox with Logging {
  val thisTile = this
  val imageData = image
  val preferedTileWidth = (thumbnailWidth + 8).toDouble
  val preferedTileHeight = (thumbnailWidth + 32).toDouble
  //set tile size
  this.setPrefSize(preferedTileWidth, preferedTileHeight)
  this.setMinSize(preferedTileWidth, preferedTileHeight)
  this.setMaxSize(preferedTileWidth, preferedTileHeight)

  //set padding on the tiles
  //this.setPadding(new Insets(10.0d,0.0d,10.0d,0.0d))

  this.setAlignment(Pos.CENTER)
  this.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler[MouseEvent] {
    override def handle(event: MouseEvent): Unit = {
      if (event.isShiftDown) {
        //multiple selection
        imageTilePane.addImageSelected(thisTile)
      }
      else {
        if (event.isPrimaryButtonDown) {
          imageTilePane.imageSelected(thisTile)
          //double click
          if (event.getClickCount == 2) {
            // Look into http://stackoverflow.com/questions/228477/how-do-i-programmatically-determine-operating-system-in-java
            // for proper multi-platform opening support
            //Desktop.getDesktop.open(new File(image.getImagePath))
          } else {

          }
        } else if (event.isSecondaryButtonDown) {
          //right click context menu
          debug("Requesting Context Menu")
          val contextMenuEvent = new ContextMenuEvent(
            thisTile,
            thisTile,
            ContextMenuEvent.CONTEXT_MENU_REQUESTED,
            event.getX, event.getY,
            event.getScreenX, event.getScreenY,
            false,
            new PickResult(thisTile, event.getSceneX, event.getSceneY))
          imageTilePane.handleContextMenu(contextMenuEvent)
        }
      }
    }
  })

  //Separator
  val separator = new Separator()
  separator.setOrientation(Orientation.HORIZONTAL)
  separator.setMaxHeight(5.0d)
  separator.setVisible(false)
  this.getChildren.add(separator)

  // Image
  val genImageView = new ImageView()
  debug(s"Getting thumbnail from: ${image.getThumbnailPath}")
  managed(new FileInputStream(image.getThumbnailPath)) acquireAndGet {
    thumbSource =>
      val thumbnail = new javafx.scene.image.Image(thumbSource)
      genImageView.setImage(thumbnail)
      if (thumbnail.getHeight > thumbnail.getWidth) {
        genImageView.setFitHeight(128.0)
      } else {
        genImageView.setFitWidth(128.0)
      }
  }
  genImageView.setPreserveRatio(true)

  this.getChildren.add(genImageView)

  //Label
  val imageLabel = new Label()
  imageLabel.setText(s"${image.getHeight}x${image.getWidth}")
  imageLabel.setWrapText(true)
  imageLabel.setMaxHeight(32d)
  imageLabel.setMaxWidth(preferedTileWidth-2)
  imageLabel.setAlignment(Pos.BOTTOM_CENTER)

  //Tooltip
  val tooltip = new Tooltip()
  tooltip.setText(s"${image.getName}")
  imageLabel.setTooltip(tooltip)
  this.getChildren.add(imageLabel)

  //this.setOnContextMenuRequested(new EventHandler[ContextMenuEvent] {
  //  override def handle(event: ContextMenuEvent): Unit = {
  //    imageTilePane.handleContextMenu(event)
  //  }
  //})

  def getImageData: com.sothr.imagetools.engine.image.Image = {
    imageData
  }
}
