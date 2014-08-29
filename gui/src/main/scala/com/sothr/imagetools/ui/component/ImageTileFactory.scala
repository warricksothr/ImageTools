package com.sothr.imagetools.ui.component

import javafx.geometry.Insets

import com.sothr.imagetools.engine.util.PropertiesService
import grizzled.slf4j.Logging

/**
 * Created by drew on 8/6/14.
 *
 * Creates pre-generated image tiles that can be rendered to a scene
 */
object ImageTileFactory extends Logging {

  def get(image: com.sothr.imagetools.engine.image.Image): ImageTile = {
    val thumbnailWidth = PropertiesService.get("app.thumbnail.size","128").toInt
    val imageTile = new ImageTile(thumbnailWidth, image)
    //set padding
    imageTile.setPadding(new Insets(2, 2, 2, 2))

    imageTile
  }

}
