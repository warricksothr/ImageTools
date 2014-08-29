package com.sothr.imagetools.ui.component

import java.io.FileInputStream
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.control.{Tooltip, Label}
import javafx.scene.image.{ImageView}
import javafx.scene.input.MouseEvent
import javafx.scene.layout.VBox

import grizzled.slf4j.Logging
import resource._

/**
 * ImageTile class that is a special VBox
 *
 * Created by drew on 8/22/14.
 */
class ImageTile(thumbnailWidth: Integer, image: com.sothr.imagetools.engine.image.Image) extends VBox with Logging {
  val imageData: com.sothr.imagetools.engine.image.Image = image
  val preferedTileSize = (thumbnailWidth + 32).toDouble
  //set tile size
  this.setPrefSize(preferedTileSize, preferedTileSize)
  this.setMinSize(preferedTileSize, preferedTileSize)
  this.setMaxSize(preferedTileSize, preferedTileSize)

  this.setAlignment(Pos.TOP_CENTER)
  this.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler[MouseEvent] {
    override def handle(event: MouseEvent): Unit = {
      if (event.isPrimaryButtonDown) {
        //double click
        if (event.getClickCount == 2) {
          // Look into http://stackoverflow.com/questions/228477/how-do-i-programmatically-determine-operating-system-in-java
          // for proper multi-platform opening support
          //Desktop.getDesktop.open(new File(image.getImagePath))
        } else {

        }
      } else if (event.isSecondaryButtonDown) {
        //right click context menu
      }
    }
  })

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

  //Tooltip
  val tooltip = new Tooltip()
  tooltip.setText(s"${image.getName}")
  imageLabel.setTooltip(tooltip)
  this.getChildren.add(imageLabel)

  def getImageData: com.sothr.imagetools.engine.image.Image = {
    imageData
  }
}
