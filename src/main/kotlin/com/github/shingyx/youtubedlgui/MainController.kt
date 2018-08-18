package com.github.shingyx.youtubedlgui

import com.github.shingyx.youtubedlgui.lib.Config
import com.github.shingyx.youtubedlgui.lib.DownloadTask
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.control.Alert
import javafx.scene.control.TableView
import javafx.scene.control.TextField
import javafx.scene.input.Clipboard
import javafx.stage.Modality
import javafx.stage.Stage
import org.apache.commons.httpclient.util.URIUtil
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

class MainController {
    @FXML
    private lateinit var urlField: TextField
    @FXML
    private lateinit var table: TableView<DownloadTask>

    private val tasks = ConcurrentHashMap<String, DownloadTask>()
    private val executorService = Executors.newFixedThreadPool(8)

    init {
        try {
            Config.load()
        } catch (e: IOException) {
            showError("Loading config failed")
        }
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
    private fun pasteUrl() {
        val clipboardText = Clipboard.getSystemClipboard().string
        if (!clipboardText.isNullOrBlank()) {
            urlField.text = clipboardText.trim()
        }
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
