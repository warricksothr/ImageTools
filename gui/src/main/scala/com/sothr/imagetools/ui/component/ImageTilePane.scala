package com.sothr.imagetools.ui.component

import java.util
import javafx.collections.{ModifiableObservableListBase, ObservableList}
import javafx.scene.control.MultipleSelectionModel
import javafx.scene.layout.TilePane

/**
 * Custom Tile Pane with a multi selection model
 *
 * Created by drew on 8/29/14.
 */
class ImageTilePane extends TilePane {
  val selectionModel = new ImageTilePaneSelectionModel(this)
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
    this.selectedIndexes.clear()
    this.selectedIndexes.add(index)
    for (i <- indices) {
      this.selectedIndexes.add(i)
    }
  }

  override def selectAll(): Unit = {
    this.selectedIndexes.clear()
    for (index <- 0 until this.parentTilePane.getChildren.size()) {
      this.selectedIndexes.add(index)
    }
  }

  override def selectFirst(): Unit = {
    this.selectedIndexes.clear()
    this.selectedIndexes.add(0)
  }

  override def selectLast(): Unit = {
    this.selectedIndexes.clear()
    this.selectedIndexes.add(this.parentTilePane.getChildren.size()-1)
  }

  override def clearAndSelect(index: Int): Unit = {
    this.selectedIndexes.clear()
    this.selectedIndexes.add(index)
  }

  override def clearSelection(index: Int): Unit = {
    this.selectedIndexes.remove(index)
  }

  override def clearSelection(): Unit = {
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
    this.selectedIndexes.clear()
    this.selectedIndexes.add(index)
  }

  override def select(obj: ImageTile): Unit = {
    if (this.parentTilePane.getChildren.contains(obj)) {
      this.selectedIndexes.clear()
      this.selectedIndexes.add(this.parentTilePane.getChildren.indexOf(obj))
    }
  }

  override def isEmpty: Boolean = {
    this.parentTilePane.getChildren.isEmpty
  }

  override def isSelected(index: Int): Boolean = {
    this.selectedIndexes.contains(index)
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