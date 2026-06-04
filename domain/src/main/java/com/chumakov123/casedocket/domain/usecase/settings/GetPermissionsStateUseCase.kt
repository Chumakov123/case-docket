package com.chumakov123.casedocket.domain.usecase.settings

import com.chumakov123.casedocket.domain.model.PermissionsState
import com.chumakov123.casedocket.domain.repository.PermissionRepository
import kotlinx.coroutines.flow.Flow

class GetPermissionsStateUseCase(
    private val repository: PermissionRepository
) {
    operator fun invoke(): Flow<PermissionsState> = repository.observePermissionsState()
}
