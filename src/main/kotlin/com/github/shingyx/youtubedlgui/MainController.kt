package com.github.shingyx.youtubedlgui

import javafx.application.Platform
import javafx.concurrent.Task
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.control.Alert
import javafx.scene.control.Button
import javafx.scene.control.TextField
import javafx.scene.input.Clipboard
import javafx.stage.Modality
import javafx.stage.Stage
import org.apache.commons.httpclient.util.URIUtil
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL

class MainController {
    @FXML
    private lateinit var urlField: TextField
    @FXML
    private lateinit var startDownloadButton: Button

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
                val validUrl = try {
                    URL(clipboardText)
                    true
                } catch (e: MalformedURLException) {
                    false
                }
                if (validUrl) {
                    urlField.text = clipboardText
                }
            }
        }
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
        }
        try {
            URL(input)
        } catch (e: MalformedURLException) {
            return showError("$input is not a valid YouTube URL")
        }
        val url = URIUtil.encodeQuery(input)

        startDownloadButton.isDisable = true

        val format = "bestvideo[ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]"
        val processBuilder = ProcessBuilder(
                "\"${Config.youtubeDlPath}\"",
                "--ffmpeg-location",
                "\"${Config.ffmpegPath}\"",
                "-o",
                "\"${Config.outputDir}/%(title)s.%(ext)s\"",
                "-f",
                "\"$format\"",
                "--newline",
                url
        )
        processBuilder.redirectErrorStream(true)

        val task = object : Task<Unit>() {
            override fun call() {
                val process = processBuilder.start()
                process.inputStream.use { stream ->
                    stream.bufferedReader().use { reader ->
                        reader.forEachLine { line ->
                            println(line)
                        }
                    }
                }
                Platform.runLater {
                    startDownloadButton.isDisable = false
                }
            }
        }
        Thread(task).start()
    }
}
