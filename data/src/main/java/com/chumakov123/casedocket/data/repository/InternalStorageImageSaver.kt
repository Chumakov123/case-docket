package com.chumakov123.casedocket.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.chumakov123.casedocket.domain.repository.ImageSaver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class InternalStorageImageSaver(
    private val context: Context
) : ImageSaver {
    override suspend fun save(imageBytes: ByteArray, nameHint: String?): String? = withContext(
        Dispatchers.IO) {
        try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.getDefault()).format(Date())
            val baseName = nameHint?.substringBeforeLast('.') ?: "image"
            val extension = nameHint?.substringAfterLast('.', "")?.takeIf { it.isNotBlank() } ?: "png"
            val fileName = "${baseName}_$timestamp.$extension"
            val file = File(context.filesDir, fileName)
            file.writeBytes(imageBytes)
            Uri.fromFile(file).toString()
        } catch (e: Exception) {
            Log.e("InternalStorageImageSaver", "Failed to save image", e)
            null
        }
    }
}