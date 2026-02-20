package com.chumakov123.casedocket.domain.repository

import com.chumakov123.casedocket.domain.model.imaging.ImageRegion

interface OcrService {
    suspend fun recognizeTextInRegion(
        imageBytes: ByteArray,
        region: ImageRegion? = null
    ): String
}

