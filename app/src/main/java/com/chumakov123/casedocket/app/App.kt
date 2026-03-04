package com.chumakov123.casedocket.app

import android.app.Application
import com.chumakov123.casedocket.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.opencv.android.OpenCVLoader

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        OpenCVLoader.initLocal()

        startKoin {
            androidContext(this@App)
            modules(appModules)
        }
    }
}