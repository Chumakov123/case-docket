package com.chumakov123.casedocket.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.chumakov123.casedocket.BuildConfig
import com.chumakov123.casedocket.R
import com.chumakov123.casedocket.di.appModules
import com.chumakov123.casedocket.presentation.tracker.AppForegroundTracker
import org.koin.android.ext.android.getKoin
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@App)
            modules(appModules)
        }

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        getKoin().get<AppForegroundTracker>()

        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "upcoming_cases_channel",
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}