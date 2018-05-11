package com.github.shingyx.youtubedlgui

import javafx.scene.control.Alert

fun showError(message: String) {
    val alert = Alert(Alert.AlertType.ERROR)
    alert.headerText = null
    alert.contentText = message
    alert.show()
}
