package org.example.project.di

import di.appModule
import org.koin.core.context.startKoin

fun initKoin() = startKoin {
    modules(appModule, viewModelModule)
}
