package com.github.shingyx.youtubedlgui.lib

import javafx.application.Platform
import javafx.beans.property.SimpleStringProperty
import javafx.concurrent.Task
import java.io.IOException
import java.net.URL
import java.net.URLDecoder
import javafx.beans.property.ReadOnlyStringProperty

class DownloadTask(private val url: String) : Task<Unit>() {
    private val processBuilder: ProcessBuilder
    private var videoId: String? = null

    private val speed = SimpleStringProperty(this, "speed", "-")
    private val eta = SimpleStringProperty(this, "eta", "-")

    init {
        val format = "bestvideo[ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]"
        processBuilder = ProcessBuilder(
                "\"${Config.youtubeDlPath}\"",
                "--ffmpeg-location",
                "\"${Config.ffmpegPath}\"",
                "--output",
                "\"${Config.outputDir}/%(title)s.%(ext)s\"",
                "--format",
                "\"$format\"",
                "--newline",
                url
        )
        processBuilder.redirectErrorStream(true)
    }

    override fun call() {
        updateTitle(url)
        updateMessage("Initializing")

        val process = processBuilder.start()
        var completeMessage = "Complete"

        process.inputStream.bufferedReader().forEachLine {
            if (isCancelled) {
                completeMessage = "Cancelled"
                process.destroy()
                return@forEachLine
            }
            completeMessage = processLine(it.trim()) ?: completeMessage
        }

        updateProgress(100, 100)
        updateMessage(completeMessage)
        updateSpeed("-")
        updateEta("-")
    }

    private fun processLine(line: String): String? {
        println(line)
        var completeMessage: String? = null
        val parts = line.split("\\s+".toRegex())
        when (parts[0]) {
            "[youtube]" -> {
                if (videoId == null) {
                    videoId = parts[1].dropLast(1)
                    // Reading international characters from the input stream doesn't always work on Windows - just request the title
                    Thread({
                        try {
                            val videoInfoUrl = URL("https://youtube.com/get_video_info?video_id=$videoId")
                            val connection = videoInfoUrl.openConnection()
                            connection.connectTimeout = 5000
                            connection.readTimeout = 5000
                            val response = connection.getInputStream().bufferedReader().use { it.readText() }
                            val decoded = URLDecoder.decode(response, Charsets.UTF_8.name())
                            val videoTitle = decoded.split("&").find { it.startsWith("title=") }?.substringAfter("=")
                            if (!videoTitle.isNullOrEmpty()) {
                                updateTitle(videoTitle)
                            }
                        } catch (e: IOException) {
                            // it tried
                        }
                    }).start()
                }
            }
            "[download]" -> {
                updateMessage("Downloading")
                if (parts[1].endsWith("%")) {
                    val percentage = Math.min(99.9, parts[1].dropLast(1).toDouble())
                    updateProgress(percentage, 100.0)
                    if (line.contains("ETA") && !line.contains("Unknown")) {
                        updateSpeed(parts[5])
                        updateEta(parts[7])
                    }
                } else if (line.matches(".+has already been downloaded( and merged)?".toRegex())) {
                    completeMessage = "Already downloaded"
                }
            }
            "ERROR:" -> {
                val errorMessage = line.substringAfter("ERROR:").trim()
                completeMessage = "Error: $errorMessage"
            }
        }
        return completeMessage
    }

    @Suppress("unused")
    fun speedProperty(): ReadOnlyStringProperty {
        return speed
    }

    private fun updateSpeed(value: String) {
        Platform.runLater({ speed.set(value) })
    }

    @Suppress("unused")
    fun etaProperty(): ReadOnlyStringProperty {
        return eta
    }

    private fun updateEta(value: String) {
        Platform.runLater({ eta.set(value) })
    }
}
