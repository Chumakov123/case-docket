package com.chumakov123.casedocket.domain.repository

import com.chumakov123.casedocket.domain.model.imaging.DocumentLayout

interface ImageLayoutAnalyzer {
    suspend fun analyze(imageBytes: ByteArray): DocumentLayout
}