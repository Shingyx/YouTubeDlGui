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
    private var downloadState: DownloadState = DownloadState.INITIALIZING
    private var completeMessage = "Complete"

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
                "--ignore-config",
                "--newline",
                url
        )
        processBuilder.redirectErrorStream(true)
    }

    override fun call() {
        updateTitle(url)
        updateMessage("Initializing")

        val process = processBuilder.start()
        process.inputStream.bufferedReader().forEachLine {
            if (isCancelled) {
                completeMessage = "Cancelled"
                process.destroy()
                return@forEachLine
            }
            processLine(it.trim())
        }

        downloadState = DownloadState.COMPLETE
        updateProgress(100, 100)
        updateMessage(completeMessage)
        updateSpeed("-")
        updateEta("-")
    }

    private fun processLine(line: String) {
        println(line)
        val parts = line.split("\\s+".toRegex())
        when (parts[0]) {
            "[youtube]" -> {
                if (videoId == null) {
                    videoId = parts[1].dropLast(1)
                    // Reading international characters from the input stream doesn't always work on Windows - just request the title
                    Thread {
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
                    }.start()
                }
            }
            "[download]" -> {
                if (parts[1] == "Destination:") {
                    if (line.endsWith(".mp4")) {
                        downloadState = DownloadState.DOWNLOADING_VIDEO
                        updateMessage("Downloading video")
                    } else if (line.endsWith(".m4a")) {
                        downloadState = DownloadState.DOWNLOADING_AUDIO
                        updateMessage("Downloading audio")
                    }
                } else if (parts[1].endsWith("%")) {
                    var percentage = Math.min(99.9, parts[1].dropLast(1).toDouble())
                    if (downloadState == DownloadState.DOWNLOADING_AUDIO) {
                        percentage = 100 + percentage / 10
                    }
                    updateProgress(percentage, 110.0)
                    if (line.contains("ETA") && !line.contains("Unknown")) {
                        updateSpeed(parts[5])
                        updateEta(parts[7])
                    }
                } else if (line.matches(".+has already been downloaded( and merged)?".toRegex())) {
                    completeMessage = "Already downloaded"
                }
            }
            "[ffmpeg]" -> {
                if (parts[1] == "Merging" && parts[2] == "formats") {
                    downloadState = DownloadState.MERGING_FORMATS
                    updateMessage("Merging video and audio")
                }
            }
            "ERROR:" -> {
                val errorMessage = line.substringAfter("ERROR:").trim()
                completeMessage = "Error: $errorMessage"
            }
        }
    }

    @Suppress("unused")
    fun speedProperty(): ReadOnlyStringProperty {
        return speed
    }

    private fun updateSpeed(value: String) {
        Platform.runLater { speed.set(value) }
    }

    @Suppress("unused")
    fun etaProperty(): ReadOnlyStringProperty {
        return eta
    }

    private fun updateEta(value: String) {
        Platform.runLater { eta.set(value) }
    }
}

private enum class DownloadState {
    INITIALIZING,
    DOWNLOADING_VIDEO,
    DOWNLOADING_AUDIO,
    MERGING_FORMATS,
    COMPLETE
}
