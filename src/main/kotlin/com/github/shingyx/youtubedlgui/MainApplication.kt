package com.github.shingyx.youtubedlgui

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.stage.Stage

class MainApplication : Application() {

    override fun start(primaryStage: Stage) {
        val loader = FXMLLoader(javaClass.getResource("Main.fxml"))
        primaryStage.scene = Scene(loader.load())
        primaryStage.title = "YouTube DL GUI"
        primaryStage.show()
    }
}

fun main(args: Array<String>) {
    Application.launch(MainApplication::class.java, *args)
}
