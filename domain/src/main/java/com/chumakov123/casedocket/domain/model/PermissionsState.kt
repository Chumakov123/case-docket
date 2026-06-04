package com.chumakov123.casedocket.domain.model

data class PermissionsState(
    val notificationsGranted: Boolean = false,
    val exactAlarmsGranted: Boolean = false,
    val batteryOptimizationsIgnored: Boolean = false
)
