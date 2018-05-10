package com.github.shingyx.youtubedlgui

import javafx.fxml.FXML
import javafx.scene.control.Alert
import javafx.scene.control.TextField
import javafx.stage.Stage
import java.io.File

class ConfigController {
    @FXML
    private lateinit var youtubeDlPathField: TextField
    @FXML
    private lateinit var ffmpegPathField: TextField
    @FXML
    private lateinit var outputDirField: TextField

    @FXML
    private fun initialize() {
        youtubeDlPathField.text = Config.youtubeDlPath
        ffmpegPathField.text = Config.ffmpegPath
        outputDirField.text = Config.outputDir
    }

    @FXML
    private fun youtubeDlPathBrowse() {
        // TODO
    }

    @FXML
    private fun ffmpegPathBrowse() {
    }

    @FXML
    private fun outputDirBrowse() {
    }

    @FXML
    private fun save() {
        val youtubeDlPath = youtubeDlPathField.text?.trim()
        val ffmpegPath = ffmpegPathField.text?.trim()
        val outputDir = outputDirField.text?.trim()

        val errors = ArrayList<String>()
        if (youtubeDlPath.isNullOrEmpty()) {
            errors.add("youtube-dl path cannot be blank")
        } else {
            val youtubeDlPathFile = File(youtubeDlPath)
            if (!youtubeDlPathFile.isFile || youtubeDlPathFile.name != "youtube-dl.exe") {
                errors.add("$youtubeDlPath is not a valid youtube-dl executable")
            }
        }
        if (ffmpegPath.isNullOrEmpty()) {
            errors.add("ffmpeg path cannot be blank")
        } else {
            val ffmpegPathFile = File(ffmpegPath)
            if (!ffmpegPathFile.isFile || ffmpegPathFile.name != "ffmpeg.exe") {
                errors.add("$ffmpegPath is not a valid ffmpeg executable")
            }
        }
        if (outputDir.isNullOrEmpty()) {
            errors.add("Output directory cannot be blank")
        } else {
            val outputDirFile = File(outputDir)
            if (!outputDirFile.isDirectory) {
                errors.add("$outputDir is not a valid output directory")
            }
        }
        if (errors.isNotEmpty()) {
            val alert = Alert(Alert.AlertType.ERROR)
            alert.headerText = "Invalid configuration"
            alert.contentText = errors.joinToString("\n")
            alert.showAndWait()
            return
        }
        // TODO save then closeWindow()
    }

    @FXML
    private fun cancel() {
        closeWindow()
    }

    private fun closeWindow() {
        val stage = youtubeDlPathField.scene.window as Stage
        stage.close()
    }
}
