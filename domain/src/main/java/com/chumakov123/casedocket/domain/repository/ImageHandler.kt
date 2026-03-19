package com.chumakov123.casedocket.domain.repository

import com.chumakov123.casedocket.domain.model.ErrorMessage

interface ImageHandler {
    suspend fun processSelectedImages(imageUris: List<String>): ProcessingResult
    suspend fun processCapturedImage(imageUri: String): ProcessingResult
}

data class ProcessingResult(
    val successCount: Int,
    val errorCount: Int,
    val errorMessage: ErrorMessage? = null
)