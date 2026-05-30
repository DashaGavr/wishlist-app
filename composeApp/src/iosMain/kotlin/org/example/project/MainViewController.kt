package org.example.project

import androidx.compose.ui.window.ComposeUIViewController
import di.initKoin
import org.example.project.di.viewModelModule
import platform.UIKit.UIViewController

private val koinInit: Unit by lazy {
    try {
        initKoin(viewModelModule)
    } catch (e: Exception) {
        println("KOIN INIT ERROR: ${e.message}")
        throw e
    }
}

fun MainViewController(): UIViewController {
    koinInit
    return ComposeUIViewController { App() }
}