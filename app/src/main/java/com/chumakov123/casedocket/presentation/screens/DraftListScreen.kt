package com.chumakov123.casedocket.presentation.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
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

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
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
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = stringResource(R.string.take_photo)
                        )
                    }
                }

                FloatingActionButton(
                    onClick = { galleryLauncher.launch("image/*") },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ) {
                    Icon(
                        Icons.Default.PhotoLibrary,
                        contentDescription = stringResource(R.string.choose_from_gallery)
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val isEmpty = taskGroups.pendingProcessing.isEmpty() &&
                    taskGroups.completed.isEmpty() &&
                    taskGroups.failed.isEmpty()

            AnimatedVisibility(
                visible = isEmpty,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    EmptyState(
                        message = stringResource(R.string.no_images_message),
                        icon = Icons.Default.PhotoLibrary,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp)
                    )
                }
            }

            if (!isEmpty) {
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
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        if (taskGroups.pendingProcessing.isNotEmpty()) {
                            item(key = "header_pending") {
                                ListHeader(text = stringResource(R.string.pending_section))
                            }
                            items(
                                items = taskGroups.pendingProcessing,
                                key = { it.id }
                            ) { task ->
                                PendingTaskItem(task = task)
                            }
                        }

                        if (taskGroups.completed.isNotEmpty()) {
                            item(key = "header_ready") {
                                ListHeader(text = stringResource(R.string.ready_section))
                            }
                            items(
                                items = taskGroups.completed,
                                key = { it.id }
                            ) { task ->
                                DraftTaskItem(
                                    task = task,
                                    onEditClick = { onNavigateToEdit(task.id) }
                                )
                            }
                        }

                        if (taskGroups.failed.isNotEmpty()) {
                            item(key = "header_failed") {
                                ListHeader(text = stringResource(R.string.failed_section))
                            }
                            items(
                                items = taskGroups.failed,
                                key = { it.id }
                            ) { task ->
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
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
fun ListHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.2.sp,
        modifier = modifier
            .padding(top = 16.dp, bottom = 4.dp)
    )
}

@Composable
fun PendingTaskItem(task: RecognitionTask) {
    val statusText = when (task.status) {
        TaskStatus.PENDING -> stringResource(R.string.status_pending)
        TaskStatus.PROCESSING -> stringResource(R.string.status_processing)
        else -> ""
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AsyncImage(
                        model = task.imageUri,
                        contentDescription = null,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(MaterialTheme.shapes.extraSmall),
                        contentScale = ContentScale.Crop,
                        alpha = 0.6f
                    )
                    Text(
                        text = stringResource(R.string.image_number, task.id),
                        style = MaterialTheme.typography.titleSmall
                    )
                }

                Surface(
                    color = if (task.status == TaskStatus.PROCESSING)
                        MaterialTheme.colorScheme.tertiaryContainer
                    else MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.extraSmall
                ) {
                    Text(
                        text = statusText,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (task.status == TaskStatus.PROCESSING)
                            MaterialTheme.colorScheme.onTertiaryContainer
                        else MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            if (task.status == TaskStatus.PROCESSING) {
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    strokeCap = StrokeCap.Round
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DraftTaskItem(task: RecognitionTask, onEditClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onEditClick,
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AsyncImage(
                        model = task.imageUri,
                        contentDescription = null,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(MaterialTheme.shapes.small),
                        contentScale = ContentScale.Crop
                    )
                    Column {
                        Text(
                            text = stringResource(R.string.image_number, task.id),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.uploaded_label, formatDate(task.createdAt)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(R.string.edit),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            task.resultDraft?.let { draft ->
                Spacer(modifier = Modifier.height(12.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (draft.judge.text.isNotBlank()) {
                        MetadataBadge(
                            icon = Icons.Default.Person,
                            text = draft.judge.text,
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    draft.date?.let { date ->
                        MetadataBadge(
                            icon = Icons.Default.CalendarToday,
                            text = date.toDisplayFormat()
                        )
                    }

                    if (draft.cases.isNotEmpty()) {
                        MetadataBadge(
                            icon = Icons.AutoMirrored.Filled.List,
                            text = stringResource(R.string.cases_count, draft.cases.size)
                        )
                    }
                }
            }
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
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AsyncImage(
                        model = task.imageUri,
                        contentDescription = null,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(MaterialTheme.shapes.extraSmall),
                        contentScale = ContentScale.Crop,
                        alpha = 0.5f
                    )
                    Text(
                        text = stringResource(R.string.image_number, task.id),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }

                Row {
                    IconButton(onClick = onRetryClick) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.retry_button),
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
fun MetadataBadge(
    icon: ImageVector,
    text: String,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer
) {
    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(0.5.dp, contentColor.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
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
