package com.chumakov123.casedocket.data.repository

import android.content.Context
import android.net.Uri
import com.chumakov123.casedocket.domain.model.ErrorMessage
import com.chumakov123.casedocket.domain.repository.ImageHandler
import com.chumakov123.casedocket.domain.repository.ImageSaver
import com.chumakov123.casedocket.domain.repository.ProcessingResult
import com.chumakov123.casedocket.domain.service.ScheduleRecognitionManager

class ImageHandlerImpl(
    private val сontext: Context,
    private val imageSaver: ImageSaver,
    private val manager: ScheduleRecognitionManager
) : ImageHandler {

    override suspend fun processSelectedImages(imageUris: List<String>): ProcessingResult {
        var success = 0
        var error = 0
        imageUris.forEach { uriString ->
            try {
                val uri = Uri.parse(uriString)
                val bytes = сontext.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes != null) {
                    val filename = "gallery_${System.currentTimeMillis()}_${uri.hashCode()}.jpg"
                    val savedPath = imageSaver.save(bytes, filename)
                    if (savedPath != null) {
                        manager.submitImage("file://$savedPath")
                        success++
                    } else {
                        error++
                    }
                } else {
                    error++
                }
            } catch (e: Exception) {
                error++
            }
        }
        return ProcessingResult(success, error)
    }

    override suspend fun processCapturedImage(imageUri: String): ProcessingResult {
        return try {
            val uri = Uri.parse(imageUri)
            val bytes = сontext.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            if (bytes != null) {
                val filename = "camera_${System.currentTimeMillis()}.jpg"
                val savedPath = imageSaver.save(bytes, filename)
                if (savedPath != null) {
                    manager.submitImage("file://$savedPath")
                    ProcessingResult(1, 0)
                } else {
                    ProcessingResult(0, 1, ErrorMessage.PhotoSaveFailed)
                }
            } else {
                ProcessingResult(0, 1, ErrorMessage.PhotoReadFailed)
            }
        } catch (e: Exception) {
            ProcessingResult(0, 1, ErrorMessage.PhotoProcessingError(e.message ?: ""))
        } finally {
            try {
                сontext.contentResolver.delete(Uri.parse(imageUri), null, null)
            } catch (_: Exception) {
            }
        }
    }
}