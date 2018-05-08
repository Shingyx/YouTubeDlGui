package com.github.shingyx.youtubedlgui

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage

class MainApplication : Application() {

    override fun start(primaryStage: Stage) {
        val root = FXMLLoader.load<Parent>(javaClass.getResource("Main.fxml"))
        primaryStage.title = "YouTube DL GUI"
        primaryStage.scene = Scene(root, 360.0, 170.0)
        primaryStage.show()
    }
}

fun main(args: Array<String>) {
    Application.launch(MainApplication::class.java, *args)
}
