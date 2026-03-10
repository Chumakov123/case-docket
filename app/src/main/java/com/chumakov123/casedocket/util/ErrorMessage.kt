package com.chumakov123.casedocket.util

sealed class ErrorMessage {
    object DraftNotFound : ErrorMessage()
    object ScheduleNotFound : ErrorMessage()
    data class LoadingError(val details: String) : ErrorMessage()
    data class ConfirmationError(val details: String) : ErrorMessage()
    data class RejectionError(val details: String) : ErrorMessage()
    data class SaveError(val details: String) : ErrorMessage()
    data class UploadSummary(val success: Int, val errors: Int) : ErrorMessage()
    object PhotoSaveFailed : ErrorMessage()
    object PhotoReadFailed : ErrorMessage()
    data class PhotoProcessingError(val details: String) : ErrorMessage()
    object CameraPermissionRequired : ErrorMessage()
}