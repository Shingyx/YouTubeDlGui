package com.github.shingyx.youtubedlgui

import javafx.application.Platform
import javafx.concurrent.Task
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Alert
import javafx.scene.control.Button
import javafx.scene.control.TextField
import javafx.stage.Modality
import javafx.stage.Stage
import org.apache.commons.httpclient.util.URIUtil
import java.net.MalformedURLException
import java.net.URL
import java.nio.file.Paths

class MainController {
    @FXML
    private lateinit var urlField: TextField
    @FXML
    private lateinit var startDownloadButton: Button

    init {
        println("Current relative path is: ${Paths.get("").toAbsolutePath()}")
        Config.load()
        println(Config)
    }

    @FXML
    private fun openConfiguration() {
        val stage = Stage()
        val root = FXMLLoader.load<Parent>(javaClass.getResource("Config.fxml"))
        stage.title = "Configure paths"
        stage.scene = Scene(root)
        stage.initModality(Modality.APPLICATION_MODAL)
        stage.show()
    }

    @FXML
    private fun startDownload() {
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

    private fun showError(message: String) {
        val alert = Alert(Alert.AlertType.ERROR)
        alert.headerText = null
        alert.contentText = message
        alert.showAndWait()
    }
}
