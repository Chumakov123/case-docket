package com.chumakov123.casedocket.domain.repository

interface ImagePreprocessor {
    suspend fun preprocess(imageBytes: ByteArray): ByteArray
}