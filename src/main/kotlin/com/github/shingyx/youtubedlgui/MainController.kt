package com.github.shingyx.youtubedlgui

import com.github.shingyx.youtubedlgui.lib.Config
import com.github.shingyx.youtubedlgui.lib.DownloadTask
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
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
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainController {
    @FXML
    private lateinit var container: VBox
    @FXML
    private lateinit var urlField: TextField

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

        val titleColumn = TableColumn<DownloadTask, String>("Video")
        titleColumn.cellValueFactory = PropertyValueFactory<DownloadTask, String>("title")
        val progressColumn = TableColumn<DownloadTask, Double>("Progress")
        progressColumn.cellValueFactory = PropertyValueFactory<DownloadTask, Double>("progress")
        progressColumn.cellFactory = ProgressBarTableCell.forTableColumn()
        val statusColumn = TableColumn<DownloadTask, String>("Status")
        statusColumn.cellValueFactory = PropertyValueFactory<DownloadTask, String>("message")
        table.columns.addAll(titleColumn, progressColumn, statusColumn)
        container.children.add(table)

        executorService = Executors.newFixedThreadPool(8)
    }

    @FXML
    private fun openConfiguration() {
        val stage = Stage()
        val loader = FXMLLoader(javaClass.getResource("Config.fxml"))
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

        val task = DownloadTask(url)
        table.items.add(task)
        executorService.execute(task)
    }

    fun cleanup() {
        executorService.shutdown()
    }
}
