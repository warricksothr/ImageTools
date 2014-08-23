package com.sothr.imagetools.ui.component

import javafx.scene.layout.VBox

import com.sothr.imagetools.image.Image

/**
 * ImageTile class that is a special VBox
 *
 * Created by drew on 8/22/14.
 */
class ImageTile extends VBox{
  var imageData: Image = null

  def getImageData:Image = {
    imageData
  }

  def setImageData(image:Image) = {
    this.imageData = image
  }
}
