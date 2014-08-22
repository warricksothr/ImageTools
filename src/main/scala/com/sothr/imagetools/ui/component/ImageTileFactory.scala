package com.sothr.imagetools.ui.component

import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.image.{ImageView, Image}
import javafx.scene.input.MouseEvent
import javafx.scene.layout.{Background, BackgroundFill, VBox}
import javafx.scene.paint.Color

/**
 * Created by drew on 8/6/14.
 *
 * Creates pre-generated image tiles that can be rendered to a scene
 */
object ImageTileFactory {

  def get(image:com.sothr.imagetools.image.Image):ImageTile = {
    val imageTile = new ImageTile()
    imageTile.setImageData(image)
    imageTile.setPrefSize(192.0d,192.0d)
    imageTile.setAlignment(Pos.TOP_CENTER)
    imageTile.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler[MouseEvent] {
        override def handle(event: MouseEvent): Unit = {
          if (event.isSecondaryButtonDown()) {
            //right click context menu
          }
      }
    })

    // Image
    val genImageView = new ImageView()
    val thumbnail = new Image(image.getThumbnailPath)
    genImageView.setImage(thumbnail)
    genImageView.setFitWidth(128.0)
    genImageView.setPreserveRatio(true)
    imageTile.getChildren.add(genImageView)

    //Label
    val imageLabel = new Label()
    imageLabel.setText(image.getName())
    imageLabel.setWrapText(true)
    imageTile.getChildren.add(imageLabel)

    imageTile
  }

}
