package com.chumakov123.casedocket.app

import android.app.Application
import com.chumakov123.casedocket.di.appModules
import com.chumakov123.casedocket.presentation.tracker.AppForegroundTracker
import org.koin.android.ext.android.getKoin
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

        getKoin().get<AppForegroundTracker>()
    }
}