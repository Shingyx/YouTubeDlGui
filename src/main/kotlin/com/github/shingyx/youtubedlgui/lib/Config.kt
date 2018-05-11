package com.github.shingyx.youtubedlgui.lib

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*

private const val SETTINGS_PATH = "config.properties"
private const val KEY_YOUTUBE_DL_PATH = "youtubeDlPath"
private const val KEY_FFMPEG_PATH = "ffmpegPath"
private const val KEY_OUTPUT_DIR = "outputDir"

object Config {
    private val properties = Properties()

    var youtubeDlPath: String
        get() = properties.getProperty(KEY_YOUTUBE_DL_PATH) ?: ""
        set(value) {
            properties.setProperty(KEY_YOUTUBE_DL_PATH, value)
        }
    var ffmpegPath: String
        get() = properties.getProperty(KEY_FFMPEG_PATH) ?: ""
        set(value) {
            properties.setProperty(KEY_FFMPEG_PATH, value)
        }
    var outputDir: String
        get() = properties.getProperty(KEY_OUTPUT_DIR) ?: ""
        set(value) {
            properties.setProperty(KEY_OUTPUT_DIR, value)
        }

    fun load() {
        val file = File(SETTINGS_PATH)
        if (file.isFile) {
            FileInputStream(file).use { inputStream ->
                properties.load(inputStream)
            }
        }
    }

    fun save() {
        FileOutputStream(SETTINGS_PATH).use { outputStream ->
            properties.store(outputStream, null)
        }
    }

    fun configIsValid(): Boolean {
        val youtubeDlPathFile = File(youtubeDlPath)
        if (!youtubeDlPathFile.isFile || youtubeDlPathFile.name != "youtube-dl.exe") {
            return false
        }
        val ffmpegPathFile = File(ffmpegPath)
        if (!ffmpegPathFile.isFile || ffmpegPathFile.name != "ffmpeg.exe") {
            return false
        }
        val outputDirFile = File(outputDir)
        if (!outputDirFile.isDirectory) {
            return false
        }
        return true
    }

    override fun toString(): String {
        return "Config(youtubeDlPath=$youtubeDlPath,ffmpegPath=$ffmpegPath,outputDir=$outputDir)"
    }
}
