package com.sothr.imagetools.ui.component

import java.util
import javafx.application.Platform
import javafx.collections.{ModifiableObservableListBase, ObservableList}
import javafx.event.{ActionEvent, EventHandler}
import javafx.geometry.Side
import javafx.scene.Node
import javafx.scene.control.{ContextMenu, MenuItem, MultipleSelectionModel}
import javafx.scene.input.ContextMenuEvent
import javafx.scene.layout._
import javafx.scene.paint.Color

import com.sothr.imagetools.engine.AppConfig
import com.sothr.imagetools.ui.controller.AppController
import grizzled.slf4j.Logging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.util.{Failure, Success}

/**
 * Custom Tile Pane with a multi selection model
 *
 * Created by drew on 8/29/14.
 */
class ImageTilePane extends TilePane with Logging {
  val selectionModel = new ImageTilePaneSelectionModel[ImageTile](this)

  def handleContextMenu(event: ContextMenuEvent) = {
    //Build and show a context menu
    debug("Context Menu Request Received")
    val numSelected = this.selectionModel.getSelectedIndices.size()
    if (numSelected > 0) {
      if (numSelected == 1) {
        val contextMenu = getSingleSelectionContextMenu
        debug("Showing context menu")
        contextMenu.show(event.getTarget.asInstanceOf[Node], Side.RIGHT, 0d, 0d)
      } else {
        val contextMenu = getMulipleSelectionContextMenu
        debug("Showing context menu")
        contextMenu.show(event.getTarget.asInstanceOf[Node], Side.RIGHT, 0d, 0d)
      }
    }
  }

  def getSingleSelectionContextMenu: ContextMenu = {
    debug("Building single-selection context menu")
    val contextMenu = new ContextMenu()
    val delete = new MenuItem("Delete")
    delete.setOnAction(new EventHandler[ActionEvent]() {
      def handle(e: ActionEvent) = {
        debug("Requesting Single Delete")
        deleteSelected()
      }
    })
    contextMenu.getItems.addAll(delete)
    contextMenu
  }

  def getMulipleSelectionContextMenu: ContextMenu = {
    debug("Building multi-selection context menu")
    val contextMenu = new ContextMenu()
    val delete = new MenuItem("Delete")
    delete.setOnAction(new EventHandler[ActionEvent]() {
      def handle(e: ActionEvent) = {
        debug("Requesting Multiple Delete")
        deleteSelected()
      }
    })
    contextMenu.getItems.addAll(delete)
    contextMenu
  }

  def imageSelected(imageTile: ImageTile) = {
    this.selectionModel.clearAndSelect(this.getChildren.indexOf(imageTile))
  }

  def addImageSelected(imageTile: ImageTile) = {
    this.selectionModel.select(this.getChildren.indexOf(imageTile))
  }

  def removeImageSelected(imageTile: ImageTile) = {
    this.selectionModel.clearSelection(this.getChildren.indexOf(imageTile))
  }

  def clearSelection() = {
    this.selectionModel.clearSelection()
  }

  //request deletion of selected images
  def deleteSelected() = {
    val f: Future[Unit] = Future {
      val selected = this.selectionModel.getSelectedItems
      val iterator = selected.iterator()
      while (iterator.hasNext) {
        val item = iterator.next()
        val imageTile = item.asInstanceOf[ImageTile]
        val data = imageTile.imageData
        val controller = AppConfig.getFxmlLoader.getController[AppController]()
        controller.engine.deleteImage(data)

      }
      val pane = this
      Platform.runLater(new Runnable() {
        override def run(): Unit = {
          //remove from the current panel
          pane.getChildren.removeAll(selected)
          clearSelection()
        }
      })
    }
    f onComplete {
      case Success(a) => info("Successfully deleted files")
      case Failure(f) => error("Failed to delete files", f)
    }
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
    setSelectionFormatting(this.parentTilePane.getChildren.size() - 1)
    this.selectedIndexes.add(this.parentTilePane.getChildren.size() - 1)
  }

  override def clearAndSelect(index: Int): Unit = {
    clearSelectionFormatting
    this.selectedIndexes.clear()
    setSelectionFormatting(index)
    this.selectedIndexes.add(index)
  }

  override def clearSelection(index: Int): Unit = {
    val i = this.selectedIndexes.indexOf(index)
    if (i >= 0) {
      clearSelectionFormatting(index)
      this.selectedIndexes.remove(i)
    }
  }

  private def clearSelectionFormatting(index: Int) = {
    val tile = this.parentTilePane.getChildren.get(index).asInstanceOf[VBox]
    tile.setBorder(Border.EMPTY)
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
      val nextIndex = if (currentIndex >= this.parentTilePane.getChildren.size - 1) this.parentTilePane.getChildren.size - 1 else currentIndex + 1
      this.selectedIndexes.set(0, nextIndex)
    }
  }

  override def select(index: Int): Unit = {
    //can only select once
    if (!this.selectedIndexes.contains(index)) {
      setSelectionFormatting(index)
      this.selectedIndexes.add(index)
    }
  }

  private def setSelectionFormatting(index: Int): Unit = {
    setSelectionFormatting(this.parentTilePane.getChildren.get(index).asInstanceOf[ImageTile])
  }

  private def setSelectionFormatting(imageTile: ImageTile) = {
    val borderStroke = new BorderStroke(Color.BLUE, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderStroke.THIN)
    imageTile.asInstanceOf[VBox].setBorder(new Border(borderStroke))
  }

  override def select(obj: ImageTile): Unit = {
    if (this.parentTilePane.getChildren.contains(obj)) {
      clearSelectionFormatting
      this.selectedIndexes.clear()
      setSelectionFormatting(obj)
      this.selectedIndexes.add(this.parentTilePane.getChildren.indexOf(obj))
    }
  }

  private def clearSelectionFormatting() = {
    val iterator = this.parentTilePane.getChildren.iterator()
    while (iterator.hasNext) {
      //remove the selection styling
      val imageTile: VBox = iterator.next().asInstanceOf[VBox]
      imageTile.setBorder(Border.EMPTY)
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

  def doAdd(index: Int, element: E) = {
    delegate.add(index, element)
  }

  def doSet(index: Int, element: E): E = {
    delegate.set(index, element)
  }

  def doRemove(index: Int): E = {
    delegate.remove(index)
  }

}