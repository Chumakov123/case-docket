package com.chumakov123.casedocket.domain.repository

import com.chumakov123.casedocket.domain.model.PermissionsState
import kotlinx.coroutines.flow.Flow

interface PermissionRepository {
    fun observePermissionsState(): Flow<PermissionsState>
    fun refreshPermissions()
}
