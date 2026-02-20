package com.chumakov123.casedocket.data.repository

import android.content.Context
import com.chumakov123.casedocket.domain.repository.ImageSaver
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class InternalStorageImageSaver(
    private val context: Context
) : ImageSaver {
    override suspend fun save(imageBytes: ByteArray, nameHint: String?): String? {
        return try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = (nameHint ?: "image") + "_$timestamp.png"
            val file = File(context.filesDir, fileName)
            file.writeBytes(imageBytes)
            file.absolutePath
        } catch (e: Exception) {
            // логирование ошибки
            null
        }
    }
}