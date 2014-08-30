package com.sothr.imagetools.ui.component

import java.util
import javafx.collections.{ModifiableObservableListBase, ObservableList}
import javafx.scene.control.MultipleSelectionModel
import javafx.scene.layout._
import javafx.scene.paint.{Color, Paint}

/**
 * Custom Tile Pane with a multi selection model
 *
 * Created by drew on 8/29/14.
 */
class ImageTilePane extends TilePane {
  val selectionModel = new ImageTilePaneSelectionModel(this)

  def imageSelected(imageTile: ImageTile) = {
    this.selectionModel.clearAndSelect(this.getChildren.indexOf(imageTile))
  }

  def addImageSelected(imageTile: ImageTile) = {
    this.selectionModel.select(this.getChildren.indexOf(imageTile))
  }
}

/**
 * Multiple selection model for ImageTilePane
 *
 */
class ImageTilePaneSelectionModel[ImageTile](parentTilePane: ImageTilePane) extends MultipleSelectionModel[ImageTile] {

  val selectedIndexes: ObservableList[Integer] = new ArrayObservableList[Integer]()

  override def getSelectedIndices: ObservableList[Integer] = {
    this.selectedIndexes
  }

  override def getSelectedItems: ObservableList[ImageTile] = {
    val selected = new ArrayObservableList[ImageTile]()
    val iterator = selectedIndexes.iterator()
    while (iterator.hasNext) {
      selected.add(this.parentTilePane.getChildren.get(iterator.next()).asInstanceOf[ImageTile])
    }
    selected
  }

  override def selectIndices(index: Int, indices: Int*): Unit = {
    clearSelectionFormatting
    this.selectedIndexes.clear()
    setSelectionFormatting(index)
    this.selectedIndexes.add(index)
    for (i <- indices) {
      setSelectionFormatting(i)
      this.selectedIndexes.add(i)
    }
  }

  override def selectAll(): Unit = {
    clearSelectionFormatting
    this.selectedIndexes.clear()
    for (index <- 0 until this.parentTilePane.getChildren.size()) {
      setSelectionFormatting(index)
      this.selectedIndexes.add(index)
    }
  }

  override def selectFirst(): Unit = {
    clearSelectionFormatting
    this.selectedIndexes.clear()
    setSelectionFormatting(0)
    this.selectedIndexes.add(0)
  }

  override def selectLast(): Unit = {
    clearSelectionFormatting
    this.selectedIndexes.clear()
    setSelectionFormatting(this.parentTilePane.getChildren.size()-1)
    this.selectedIndexes.add(this.parentTilePane.getChildren.size()-1)
  }

  override def clearAndSelect(index: Int): Unit = {
    clearSelectionFormatting
    this.selectedIndexes.clear()
    setSelectionFormatting(index)
    this.selectedIndexes.add(index)
  }

  override def clearSelection(index: Int): Unit = {
    this.selectedIndexes.remove(index)
  }

  override def clearSelection(): Unit = {
    clearSelectionFormatting
    this.selectedIndexes.clear()
  }

  override def selectPrevious(): Unit = {
    if (this.selectedIndexes.size == 1) {
      val currentIndex = this.selectedIndexes.get(0)
      val nextIndex = if (currentIndex < 1) 0 else currentIndex - 1
      this.selectedIndexes.set(0, nextIndex)
    }
  }

  override def selectNext(): Unit = {
    if (this.selectedIndexes.size == 1) {
      val currentIndex = this.selectedIndexes.get(0)
      val nextIndex = if (currentIndex >= this.parentTilePane.getChildren.size-1) this.parentTilePane.getChildren.size-1 else currentIndex + 1
      this.selectedIndexes.set(0, nextIndex)
    }
  }

  override def select(index: Int): Unit = {
    setSelectionFormatting(index)
    this.selectedIndexes.add(index)
  }

  override def select(obj: ImageTile): Unit = {
    if (this.parentTilePane.getChildren.contains(obj)) {
      clearSelectionFormatting
      this.selectedIndexes.clear()
      setSelectionFormatting(obj)
      this.selectedIndexes.add(this.parentTilePane.getChildren.indexOf(obj))
    }
  }

  override def isEmpty: Boolean = {
    this.parentTilePane.getChildren.isEmpty
  }

  override def isSelected(index: Int): Boolean = {
    this.selectedIndexes.contains(index)
  }

  private def clearSelectionFormatting = {
    val iterator = this.parentTilePane.getChildren.iterator()
    while (iterator.hasNext) {
      //remove the selection styling
      val imageTile: VBox = iterator.next().asInstanceOf[VBox]
      imageTile.setBorder(Border.EMPTY)
    }
  }

  private def setSelectionFormatting(index: Int): Unit = {
    setSelectionFormatting(this.parentTilePane.getChildren.get(index).asInstanceOf[ImageTile])
  }

  private def setSelectionFormatting(imageTile: ImageTile): Unit = {
    val borderStroke = new BorderStroke(Color.BLUE, BorderStrokeStyle.SOLID, CornerRadii.EMPTY,BorderStroke.THIN)
    imageTile.asInstanceOf[VBox].setBorder(new Border(borderStroke))
  }

}

class ArrayObservableList[E] extends ModifiableObservableListBase[E] {

  val delegate: util.ArrayList[E] = new util.ArrayList[E]()

  def get(index: Int): E = {
    delegate.get(index)
  }

  def size = {
    delegate.size
  }

  def doAdd (index: Int, element: E) = {
    delegate.add(index, element)
  }

  def doSet (index: Int, element: E): E = {
    delegate.set(index, element)
  }

  def doRemove (index: Int): E = {
    delegate.remove(index)
  }

}