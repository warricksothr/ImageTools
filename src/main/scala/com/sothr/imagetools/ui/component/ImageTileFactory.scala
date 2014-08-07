package com.sothr.imagetools.ui.component

import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.image.{ImageView, Image}
import javafx.scene.layout.{VBox}

/**
 * Created by drew on 8/6/14.
 *
 * Creates pre-generated image tiles that can be rendered to a scene
 */
object ImageTileFactory {

  def get(image:com.sothr.imagetools.image.Image):VBox = {
    val imageTile = new VBox()
    imageTile.setPrefSize(192.0d,192.0d)
    imageTile.setAlignment(Pos.TOP_CENTER)

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
