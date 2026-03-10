package com.chumakov123.casedocket.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.chumakov123.casedocket.R

@Composable
fun ErrorMessage.toDisplayString(): String {
    return when (this) {
        is ErrorMessage.DraftNotFound -> stringResource(R.string.draft_not_found)
        is ErrorMessage.ScheduleNotFound -> stringResource(R.string.schedule_not_found)
        is ErrorMessage.LoadingError -> stringResource(R.string.loading_error, details)
        is ErrorMessage.ConfirmationError -> stringResource(R.string.confirmation_error, details)
        is ErrorMessage.RejectionError -> stringResource(R.string.rejection_error, details)
        is ErrorMessage.DeletionError -> stringResource(R.string.deletion_error, details)
        is ErrorMessage.SaveError -> stringResource(R.string.save_error, details)
        is ErrorMessage.UploadSummary -> stringResource(R.string.upload_summary, success, errors)
        is ErrorMessage.PhotoSaveFailed -> stringResource(R.string.photo_save_failed)
        is ErrorMessage.PhotoReadFailed -> stringResource(R.string.photo_read_failed)
        is ErrorMessage.PhotoProcessingError -> stringResource(
            R.string.photo_processing_error,
            details
        )

        is ErrorMessage.CameraPermissionRequired -> stringResource(R.string.camera_permission_required)
    }
}