package com.github.shingyx.youtubedlgui

import javafx.application.Platform
import javafx.concurrent.Task
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.TextField
import java.net.MalformedURLException
import java.net.URL
import java.nio.file.Paths

class MainController {
    @FXML
    private lateinit var urlField: TextField
    @FXML
    private lateinit var startDownloadButton: Button

    private val settings: Settings = Settings()

    init {
        println("Current relative path is: ${Paths.get("").toAbsolutePath()}")
        settings.load()
        println(settings)
    }

    @FXML
    private fun startDownload() {
        val url = urlField.text.trim()
        try {
            URL(url)
        } catch (e: MalformedURLException) {
            println("$url is an invalid URL")
            return
        }

        startDownloadButton.isDisable = true

        val format = "bestvideo[ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]"
        val processBuilder = ProcessBuilder(
                "\"${settings.youtubeDlPath}\"",
                "--ffmpeg-location",
                "\"${settings.ffmpegPath}\"",
                "-o",
                "\"${settings.outputDir}/%(title)s.%(ext)s\"",
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
