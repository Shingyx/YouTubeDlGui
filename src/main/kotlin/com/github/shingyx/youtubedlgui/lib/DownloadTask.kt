package com.github.shingyx.youtubedlgui.lib

import javafx.concurrent.Task
import java.io.IOException
import java.net.URL
import java.net.URLDecoder

class DownloadTask(url: String) : Task<Unit>() {
    private val processBuilder: ProcessBuilder
    private var videoId: String? = null

    init {
        val format = "bestvideo[ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]"
        processBuilder = ProcessBuilder(
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
    }

    override fun call() {
        val process = processBuilder.start()
        process.inputStream.bufferedReader().use {
            try {
                var line = it.readLine()
                while (line != null && !isCancelled) {
                    processLine(line)
                    line = it.readLine()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

            if (isCancelled) {
                process.destroy()
            }
        }
    }

    private fun processLine(line: String) {
        println(line)
        val parts = line.split("\\s+".toRegex())
        when (parts[0]) {
            "[youtube]" -> {
                if (videoId == null) {
                    videoId = parts[1].dropLast(1)
                    updateTitle(videoId)
                    // Reading the real title from the input stream doesn't always work on Windows - just request it
                    Thread({
                        try {
                            val videoInfoUrl = URL("https://youtube.com/get_video_info?video_id=$videoId")
                            val response = videoInfoUrl.openStream().bufferedReader().use { it.readText() }
                            val decoded = URLDecoder.decode(response, Charsets.UTF_8.name())
                            val videoTitle = decoded.split("&").first { it.startsWith("title=") }.substringAfter("=")
                            updateTitle(videoTitle)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }).start()
                }
            }
            "[download]" -> {
                if (parts[1].endsWith("%")) {
                    var percentage = parts[1].dropLast(1).toDouble()
                    if (percentage == 100.0 && parts[1].contains(".")) {
                        // It will say exactly "100%" when it's done
                        percentage = 99.9
                    }
                    updateProgress(percentage, 100.0)
                }
            }
        }
    }
}
