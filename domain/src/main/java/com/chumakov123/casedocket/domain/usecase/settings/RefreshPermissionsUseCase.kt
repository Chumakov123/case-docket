package com.chumakov123.casedocket.domain.usecase.settings

import com.chumakov123.casedocket.domain.repository.PermissionRepository

class RefreshPermissionsUseCase(
    private val repository: PermissionRepository
) {
    operator fun invoke() = repository.refreshPermissions()
}
