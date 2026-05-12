package org.example.project

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import di.initKoin
import org.example.project.di.viewModelModule

fun main() {
    initKoin(viewModelModule)
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Wishlist",
        ) {
            App()
        }
    }
}