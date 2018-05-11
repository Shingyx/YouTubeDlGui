package com.github.shingyx.youtubedlgui

import javafx.scene.control.Alert
import java.net.MalformedURLException
import java.net.URL

fun isValidUrl(url: String?): Boolean {
    return try {
        URL(url)
        true
    } catch (e: MalformedURLException) {
        false
    }
}

fun showError(message: String) {
    val alert = Alert(Alert.AlertType.ERROR)
    alert.headerText = null
    alert.contentText = message
    alert.show()
}
