package com.github.shingyx.youtubedlgui

import javafx.geometry.Pos
import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn
import javafx.scene.control.cell.ProgressBarTableCell
import javafx.util.Callback

class ProgressBarTableCellFactory<S> : Callback<TableColumn<S, Double>, TableCell<S, Double>> {
    override fun call(param: TableColumn<S, Double>): TableCell<S, Double> {
        return ProgressBarTableCell<S>()
    }
}

class RightAlignTableCellFactory<S> : Callback<TableColumn<S, String>, TableCell<S, String>> {
    override fun call(param: TableColumn<S, String>): TableCell<S, String> {
        return object : TableCell<S, String>() {
            override fun updateItem(item: String?, empty: Boolean) {
                super.updateItem(item, empty)
                text = item
                alignment = Pos.CENTER_RIGHT
            }
        }
    }
}
