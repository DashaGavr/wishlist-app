package org.example.project

import androidx.compose.ui.window.ComposeUIViewController
import di.initKoin

private val koinStarted by lazy { initKoin() }

fun MainViewController() = ComposeUIViewController {
    koinStarted // ensures Koin is initialized exactly once
    App()
}