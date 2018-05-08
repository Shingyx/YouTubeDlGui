package com.github.shingyx.youtubedlgui

import javafx.fxml.FXML
import javafx.scene.control.TextField
import java.net.MalformedURLException
import java.net.URL
import java.nio.file.Paths

class MainController {
    @FXML
    private lateinit var urlField: TextField
    private val settings: Settings = Settings()

    init {
        println("Current relative path is: ${Paths.get("").toAbsolutePath()}")
        settings.load()
        println(settings)
    }

    @FXML
    private fun start() {
        val url = urlField.text.trim()
        try {
            URL(url)
        } catch (e: MalformedURLException) {
            println("$url is an invalid URL")
            return
        }

        val format = "bestvideo[ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]"

        val process = ProcessBuilder(
                "\"${settings.youtubeDlPath}\"",
                "--ffmpeg-location",
                "\"${settings.ffmpegPath}\"",
                "-o",
                "\"${settings.outputDir}/%(title)s.%(ext)s\"",
                "-f",
                "\"$format\"",
                "--newline",
                url
        ).start()

        process.inputStream.use { stream ->
            stream.bufferedReader().use { reader ->
                reader.forEachLine { line ->
                    println(line)
                }
            }
        }
    }
}
