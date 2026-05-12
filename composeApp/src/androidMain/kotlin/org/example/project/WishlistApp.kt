package org.example.project

import android.app.Application
import di.initKoin
import org.example.project.di.viewModelModule

class WishlistApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin(this, viewModelModule)
    }
}