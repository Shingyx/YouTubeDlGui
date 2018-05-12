package com.github.shingyx.youtubedlgui

import com.github.shingyx.youtubedlgui.lib.Config
import com.github.shingyx.youtubedlgui.lib.DownloadTask
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.control.cell.ProgressBarTableCell
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.input.Clipboard
import javafx.scene.layout.VBox
import javafx.stage.Modality
import javafx.stage.Stage
import org.apache.commons.httpclient.util.URIUtil
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainController {
    @FXML
    private lateinit var container: VBox
    @FXML
    private lateinit var urlField: TextField

    private val tasks = ConcurrentHashMap<String, DownloadTask>()
    private lateinit var table: TableView<DownloadTask>
    private lateinit var executorService: ExecutorService

    init {
        try {
            Config.load()
        } catch (e: IOException) {
            showError("Loading config failed")
        }
    }

    @FXML
    private fun initialize() {
        urlField.focusedProperty().addListener { _, _, focused ->
            if (focused && urlField.text.isEmpty()) {
                val clipboardText = Clipboard.getSystemClipboard().string?.trim()
                if (isValidUrl(clipboardText)) {
                    urlField.text = clipboardText
                }
            }
        }

        table = TableView()
        table.columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY

        class TableCellFormat : TableCell<DownloadTask, String>() {
            override fun updateItem(item: String?, empty: Boolean) {
                super.updateItem(item, empty)
                text = item
                alignment = Pos.CENTER_RIGHT
            }
        }

        val titleColumn = TableColumn<DownloadTask, String>("Video")
        titleColumn.cellValueFactory = PropertyValueFactory<DownloadTask, String>("title")
        val progressColumn = TableColumn<DownloadTask, Double>("Progress")
        progressColumn.cellValueFactory = PropertyValueFactory<DownloadTask, Double>("progress")
        progressColumn.cellFactory = ProgressBarTableCell.forTableColumn()
        val statusColumn = TableColumn<DownloadTask, String>("Status")
        statusColumn.cellValueFactory = PropertyValueFactory<DownloadTask, String>("message")
        val speedColumn = TableColumn<DownloadTask, String>("Speed")
        speedColumn.cellValueFactory = PropertyValueFactory<DownloadTask, String>("speed")
        speedColumn.setCellFactory { TableCellFormat() }
        val etaColumn = TableColumn<DownloadTask, String>("ETA")
        etaColumn.cellValueFactory = PropertyValueFactory<DownloadTask, String>("eta")
        etaColumn.setCellFactory { TableCellFormat() }
        table.columns.addAll(titleColumn, progressColumn, statusColumn, speedColumn, etaColumn)
        container.children.add(table)

        executorService = Executors.newFixedThreadPool(8)
    }

    @FXML
    private fun openConfiguration() {
        val stage = Stage()
        val loader = FXMLLoader(javaClass.getResource("/Config.fxml"))
        stage.scene = Scene(loader.load())
        stage.title = "Configure paths"
        stage.initModality(Modality.APPLICATION_MODAL)
        loader.getController<ConfigController>().stage = stage
        stage.show()
    }

    @FXML
    private fun startDownload() {
        if (!Config.configIsValid()) {
            val alert = Alert(Alert.AlertType.INFORMATION)
            alert.headerText = null
            alert.contentText = "Invalid configuration - press OK to configure paths"
            alert.showAndWait()
            return openConfiguration()
        }
        val input = urlField.text.trim()
        if (input.isEmpty()) {
            return showError("YouTube URL cannot be blank")
        } else if (!isValidUrl(input)) {
            return showError("$input is not a valid YouTube URL")
        }
        val url = URIUtil.encodeQuery(input)
        if (tasks.containsKey(url)) {
            return showError("Already downloading $url")
        }

        val task = DownloadTask(url)
        tasks[url] = task
        task.setOnSucceeded { tasks.remove(url) }
        task.setOnCancelled { tasks.remove(url) }
        task.setOnFailed { tasks.remove(url) }
        table.items.add(task)
        executorService.execute(task)
    }

    fun cleanup() {
        tasks.forEach { _, value -> value.cancel() }
        executorService.shutdownNow()
    }
}
