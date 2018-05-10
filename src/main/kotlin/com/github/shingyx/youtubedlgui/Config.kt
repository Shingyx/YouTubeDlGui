package com.github.shingyx.youtubedlgui

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
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
            FileInputStream(SETTINGS_PATH).use { inputStream ->
                try {
                    properties.load(inputStream)
                } catch (e: IOException) {
                    println("Loading settings failed")
                }
            }
        }
    }

    fun save() {
        FileOutputStream(SETTINGS_PATH).use { outputStream ->
            properties.store(outputStream, null)
        }
    }

    override fun toString(): String {
        return "Config(youtubeDlPath=$youtubeDlPath,ffmpegPath=$ffmpegPath,outputDir=$outputDir)"
    }
}
