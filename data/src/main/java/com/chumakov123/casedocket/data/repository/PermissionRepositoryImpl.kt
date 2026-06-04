package com.chumakov123.casedocket.data.repository

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import androidx.core.content.ContextCompat
import com.chumakov123.casedocket.domain.model.PermissionsState
import com.chumakov123.casedocket.domain.repository.PermissionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

class PermissionRepositoryImpl(
    context: Context
) : PermissionRepository {

    private val appContext = context.applicationContext

    private val refreshSignal = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    override fun observePermissionsState(): Flow<PermissionsState> = refreshSignal
        .onStart { emit(Unit) }
        .map { checkPermissions() }
        .flowOn(Dispatchers.Default)
        .distinctUntilChanged()

    override fun refreshPermissions() {
        refreshSignal.tryEmit(Unit)
    }

    private fun checkPermissions(): PermissionsState {
        val notificationsGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        val exactAlarmsGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            appContext.getSystemService(AlarmManager::class.java)?.canScheduleExactAlarms() ?: true
        } else {
            true
        }

        val batteryOptimizationsIgnored = appContext.getSystemService(PowerManager::class.java)
            ?.isIgnoringBatteryOptimizations(appContext.packageName) ?: true

        return PermissionsState(
            notificationsGranted = notificationsGranted,
            exactAlarmsGranted = exactAlarmsGranted,
            batteryOptimizationsIgnored = batteryOptimizationsIgnored
        )
    }
}
