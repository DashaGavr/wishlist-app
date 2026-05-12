package org.example.project

import androidx.compose.ui.window.ComposeUIViewController
import di.initKoin
import org.example.project.di.viewModelModule

private val koinStarted by lazy { initKoin(viewModelModule) }

fun MainViewController() = ComposeUIViewController {
    koinStarted
    App()
}