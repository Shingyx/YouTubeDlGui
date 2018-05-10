package com.github.shingyx.youtubedlgui

import javafx.fxml.FXML
import javafx.scene.control.Alert
import javafx.scene.control.TextField
import javafx.stage.DirectoryChooser
import javafx.stage.FileChooser
import javafx.stage.Stage
import java.io.File

class ConfigController {
    @FXML
    private lateinit var youtubeDlPathField: TextField
    @FXML
    private lateinit var ffmpegPathField: TextField
    @FXML
    private lateinit var outputDirField: TextField

    lateinit var stage: Stage

    @FXML
    private fun initialize() {
        youtubeDlPathField.text = Config.youtubeDlPath
        ffmpegPathField.text = Config.ffmpegPath
        outputDirField.text = Config.outputDir
    }

    @FXML
    private fun youtubeDlPathBrowse() {
        val fileChooser = FileChooser()
        fileChooser.title = "Set youtube-dl path"
        fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("youtube-dl", "youtube-dl.exe"))
        val initialFile = File(youtubeDlPathField.text)
        if (initialFile.isFile) {
            fileChooser.initialDirectory = File(initialFile.parent)
        }
        val file = fileChooser.showOpenDialog(stage)
        if (file != null) {
            youtubeDlPathField.text = file.absolutePath
        }
    }

    @FXML
    private fun ffmpegPathBrowse() {
        val fileChooser = FileChooser()
        fileChooser.title = "Set ffmpeg path"
        fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("ffmpeg", "ffmpeg.exe"))
        val initialFile = File(ffmpegPathField.text)
        if (initialFile.isFile) {
            fileChooser.initialDirectory = File(initialFile.parent)
        }
        val file = fileChooser.showOpenDialog(stage)
        if (file != null) {
            ffmpegPathField.text = file.absolutePath
        }
    }

    @FXML
    private fun outputDirBrowse() {
        val directoryChooser = DirectoryChooser()
        directoryChooser.title = "Set Output Directory"
        val initialDirectory = File(outputDirField.text)
        if (initialDirectory.isDirectory) {
            directoryChooser.initialDirectory = initialDirectory
        }
        val directory = directoryChooser.showDialog(stage)
        if (directory != null) {
            outputDirField.text = directory.absolutePath
        }
    }

    @FXML
    private fun save() {
        val youtubeDlPath = youtubeDlPathField.text.trim()
        val ffmpegPath = ffmpegPathField.text.trim()
        val outputDir = outputDirField.text.trim()

        val errors = ArrayList<String>()
        if (youtubeDlPath.isEmpty()) {
            errors.add("youtube-dl path cannot be blank")
        } else {
            val youtubeDlPathFile = File(youtubeDlPath)
            if (!youtubeDlPathFile.isFile || youtubeDlPathFile.name != "youtube-dl.exe") {
                errors.add("$youtubeDlPath is not a valid youtube-dl executable")
            }
        }
        if (ffmpegPath.isEmpty()) {
            errors.add("ffmpeg path cannot be blank")
        } else {
            val ffmpegPathFile = File(ffmpegPath)
            if (!ffmpegPathFile.isFile || ffmpegPathFile.name != "ffmpeg.exe") {
                errors.add("$ffmpegPath is not a valid ffmpeg executable")
            }
        }
        if (outputDir.isEmpty()) {
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
            return alert.show()
        }
        Config.youtubeDlPath = youtubeDlPath
        Config.ffmpegPath = ffmpegPath
        Config.outputDir = outputDir
        Config.save()
        stage.close()
    }

    @FXML
    private fun cancel() {
        stage.close()
    }
}
