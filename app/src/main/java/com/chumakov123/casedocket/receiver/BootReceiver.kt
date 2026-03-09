package com.chumakov123.casedocket.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.chumakov123.casedocket.domain.usecase.notification.RescheduleNotificationsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class BootReceiver : BroadcastReceiver(), KoinComponent {

    private val rescheduleUseCase: RescheduleNotificationsUseCase by inject()

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    rescheduleUseCase()
                } catch (e: Exception) {

                }
            }
        }
    }
}