package com.chumakov123.casedocket.presentation.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.chumakov123.casedocket.R
import com.chumakov123.casedocket.domain.model.ErrorMessage
import com.chumakov123.casedocket.domain.model.RecognitionTask
import com.chumakov123.casedocket.domain.model.TaskStatus
import com.chumakov123.casedocket.domain.model.court.Judge
import com.chumakov123.casedocket.domain.model.court.ScheduleDate
import com.chumakov123.casedocket.domain.model.court.draft.CourtScheduleDraft
import com.chumakov123.casedocket.presentation.screens.components.EmptyState
import com.chumakov123.casedocket.presentation.theme.AppTheme
import com.chumakov123.casedocket.presentation.viewmodel.DraftListViewModel
import com.chumakov123.casedocket.util.CameraHelper
import com.chumakov123.casedocket.util.toDisplayString
import my.nanihadesuka.compose.LazyColumnScrollbar
import my.nanihadesuka.compose.ScrollbarSettings
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DraftListScreen(
    onNavigateToEdit: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DraftListViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val taskGroups by viewModel.taskGroups.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val hasCamera = context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)

    val listState = rememberLazyListState()

    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            viewModel.processCapturedImage(tempCameraUri.toString())
        } else {
            tempCameraUri?.let { CameraHelper.deleteTempFile(context, it) }
        }
        tempCameraUri = null
    }

    fun launchCamera(uri: Uri?) {
        uri?.let { cameraLauncher.launch(it) }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchCamera(tempCameraUri)
        } else {
            viewModel.showError(ErrorMessage.CameraPermissionRequired)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            viewModel.processSelectedImages(uris.map { it.toString() })
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    val errorText = errorMessage?.toDisplayString()

    LaunchedEffect(errorText) {
        if (errorText != null) {
            snackbarHostState.showSnackbar(errorText)
            viewModel.clearErrorMessage()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        if (taskGroups.pendingProcessing.isEmpty() &&
            taskGroups.completed.isEmpty() &&
            taskGroups.failed.isEmpty()
        ) {
            EmptyState(
                message = stringResource(R.string.no_images_message),
                icon = Icons.Default.PhotoLibrary,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            LazyColumnScrollbar(
                state = listState,
                settings = ScrollbarSettings.Default.copy(
                    thumbUnselectedColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    thumbSelectedColor = MaterialTheme.colorScheme.primary,
                    scrollbarPadding = 2.dp
                )
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = 160.dp
                    )
                ) {
                    if (taskGroups.pendingProcessing.isNotEmpty()) {
                        item {
                            ListHeader(text = stringResource(R.string.pending_section))
                        }
                        items(taskGroups.pendingProcessing) { task ->
                            PendingTaskItem(task = task)
                        }
                    }

                    if (taskGroups.completed.isNotEmpty()) {
                        item {
                            ListHeader(text = stringResource(R.string.ready_section))
                        }
                        items(taskGroups.completed) { task ->
                            DraftTaskItem(
                                task = task,
                                onEditClick = { onNavigateToEdit(task.id) }
                            )
                        }
                    }

                    if (taskGroups.failed.isNotEmpty()) {
                        item {
                            ListHeader(text = stringResource(R.string.failed_section))
                        }
                        items(taskGroups.failed) { task ->
                            FailedTaskItem(
                                task = task,
                                onRetryClick = { viewModel.retryTask(task.id) },
                                onDeleteClick = { viewModel.deleteTask(task.id) }
                            )
                        }
                    }
                }
            }
        }

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp)
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 88.dp)
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (hasCamera) {
                FloatingActionButton(
                    onClick = {
                        val permission = Manifest.permission.CAMERA
                        if (ContextCompat.checkSelfPermission(
                                context,
                                permission
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            val photoFile = CameraHelper.createImageFile(context)
                            val uri = CameraHelper.getUriForFile(context, photoFile)
                            tempCameraUri = uri
                            launchCamera(uri)
                        } else {
                            tempCameraUri = null
                            permissionLauncher.launch(permission)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = stringResource(R.string.take_photo)
                    )
                }
            }

            FloatingActionButton(
                onClick = { galleryLauncher.launch("image/*") },
                containerColor = MaterialTheme.colorScheme.secondary
            ) {
                Icon(
                    Icons.Default.PhotoLibrary,
                    contentDescription = stringResource(R.string.choose_from_gallery)
                )
            }
        }
    }
}

@Composable
fun ListHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
            .padding(top = 16.dp, bottom = 4.dp)
    )
}

@Composable
fun PendingTaskItem(task: RecognitionTask) {
    val statusText = when (task.status) {
        TaskStatus.PENDING -> stringResource(R.string.status_pending)
        TaskStatus.PROCESSING -> stringResource(R.string.status_processing)
        TaskStatus.COMPLETED -> stringResource(R.string.status_completed)
        TaskStatus.FAILED -> stringResource(R.string.status_failed)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (task.status) {
                TaskStatus.PENDING -> MaterialTheme.colorScheme.secondaryContainer
                TaskStatus.PROCESSING -> MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(stringResource(R.string.image_number, task.id))
            Text(
                text = stringResource(R.string.status_label, statusText),
                style = MaterialTheme.typography.bodySmall
            )
            if (task.status == TaskStatus.PROCESSING) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
fun DraftTaskItem(task: RecognitionTask, onEditClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onEditClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(R.string.image_number, task.id),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = stringResource(R.string.uploaded_label, formatDate(task.createdAt)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                task.resultDraft?.let { draft ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (draft.judge.text.isNotBlank()) {
                            IconWithText(
                                icon = Icons.Default.Person,
                                text = draft.judge.text,
                                color = MaterialTheme.colorScheme.primary,
                                contentDescription = stringResource(R.string.judge)
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            draft.date?.let { date ->
                                IconWithText(
                                    icon = Icons.Default.CalendarToday,
                                    text = date.toDisplayFormat(),
                                    contentDescription = stringResource(R.string.date)
                                )
                            }
                            if (draft.cases.isNotEmpty()) {
                                IconWithText(
                                    icon = Icons.AutoMirrored.Filled.List,
                                    text = draft.cases.size.toString(),
                                    contentDescription = stringResource(R.string.cases_count)
                                )
                            }
                        }
                    }
                }
            }

            Icon(
                Icons.Default.Edit,
                contentDescription = stringResource(R.string.edit),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun FailedTaskItem(task: RecognitionTask, onRetryClick: () -> Unit, onDeleteClick: () -> Unit) {
    val errorMessage = task.errorMessage ?: stringResource(R.string.unknown_error)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(stringResource(R.string.image_number, task.id))
            Text(
                text = stringResource(R.string.error_label, errorMessage),
                style = MaterialTheme.typography.bodySmall
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onRetryClick) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = stringResource(R.string.retry_button)
                    )
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete)
                    )
                }
            }
        }
    }
}

@Composable
fun IconWithText(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    contentDescription: String?
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(16.dp),
            tint = color
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DraftTaskItemPreview() {
    AppTheme {
        Box(Modifier.padding(16.dp)) {
            DraftTaskItem(
                task = RecognitionTask(
                    id = 1,
                    imageUri = "",
                    status = TaskStatus.COMPLETED,
                    createdAt = java.util.Date(),
                    resultDraft = CourtScheduleDraft(
                        date = ScheduleDate.parse("12.12.2023"),
                        judge = Judge("Иванов Иван Иванович"),
                        cases = emptyList()
                    )
                ),
                onEditClick = {}
            )
        }
    }
}

private fun formatDate(date: java.util.Date): String {
    val format = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    return format.format(date)
}