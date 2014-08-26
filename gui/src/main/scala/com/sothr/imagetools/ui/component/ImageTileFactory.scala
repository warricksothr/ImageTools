package com.sothr.imagetools.ui.component

import java.io.FileInputStream
import javafx.event.EventHandler
import javafx.geometry.{Insets, Pos}
import javafx.scene.control.{Label, Tooltip}
import javafx.scene.image.{Image, ImageView}
import javafx.scene.input.MouseEvent

import grizzled.slf4j.Logging
import resource._

/**
 * Created by drew on 8/6/14.
 *
 * Creates pre-generated image tiles that can be rendered to a scene
 */
object ImageTileFactory extends Logging {

  def get(image: com.sothr.imagetools.engine.image.Image): ImageTile = {
    val imageTile = new ImageTile()
    imageTile.setImageData(image)
    //set tile size
    imageTile.setPrefSize(160.0d, 160.0d)
    imageTile.setMinSize(160.0d, 160.0d)
    imageTile.setMaxSize(160.0d, 160.0d)
    //set padding
    imageTile.setPadding(new Insets(2, 2, 2, 2))
    //imageTile.setSpacing(5.0d)
    imageTile.setAlignment(Pos.TOP_CENTER)
    imageTile.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler[MouseEvent] {
      override def handle(event: MouseEvent): Unit = {
        if (event.isPrimaryButtonDown) {
          //double click
          if (event.getClickCount == 2) {

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
        val thumbnail = new Image(thumbSource)
        genImageView.setImage(thumbnail)
        if (thumbnail.getHeight > thumbnail.getWidth) {
          genImageView.setFitHeight(128.0)
        } else {
          genImageView.setFitWidth(128.0)
        }
    }
    genImageView.setPreserveRatio(true)

    imageTile.getChildren.add(genImageView)

    //Label
    val imageLabel = new Label()
    imageLabel.setText(s"${image.getHeight}x${image.getWidth}")
    imageLabel.setWrapText(true)

    //Tooltip
    val tooltip = new Tooltip()
    tooltip.setText(s"${image.getName}")
    imageLabel.setTooltip(tooltip)
    imageTile.getChildren.add(imageLabel)

    imageTile
  }

}
