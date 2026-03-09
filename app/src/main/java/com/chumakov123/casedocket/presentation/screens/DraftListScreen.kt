package com.chumakov123.casedocket.presentation.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.chumakov123.casedocket.domain.model.RecognitionTask
import com.chumakov123.casedocket.domain.model.TaskStatus
import com.chumakov123.casedocket.presentation.viewmodel.DraftListViewModel
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

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            viewModel.processSelectedImages(context, uris)
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(errorMessage!!)
            viewModel.clearErrorMessage()
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Черновики") }
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (hasCamera) {
                    FloatingActionButton(
                        onClick = { /* TODO: камера */ },
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Сделать снимок")
                    }
                }

                FloatingActionButton(
                    onClick = { galleryLauncher.launch("image/*") },
                    containerColor = MaterialTheme.colorScheme.secondary
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = "Выбрать из галереи")
                }

                FloatingActionButton(
                    onClick = { viewModel.addTestImage(context) },
                    containerColor = MaterialTheme.colorScheme.tertiary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Добавить тестовое изображение")
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // Секция "В обработке"
                if (taskGroups.pendingProcessing.isNotEmpty()) {
                    item {
                        Text(
                            text = "В обработке",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                    items(taskGroups.pendingProcessing) { task ->
                        PendingTaskItem(task = task)
                    }
                }

                // Секция "Готово к проверке"
                if (taskGroups.completed.isNotEmpty()) {
                    item {
                        Text(
                            text = "Готово к проверке",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                    items(taskGroups.completed) { task ->
                        DraftTaskItem(
                            task = task,
                            onEditClick = { onNavigateToEdit(task.id) }
                        )
                    }
                }

                // Секция "Ошибки"
                if (taskGroups.failed.isNotEmpty()) {
                    item {
                        Text(
                            text = "Ошибки",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                    items(taskGroups.failed) { task ->
                        FailedTaskItem(
                            task = task,
                            onRetryClick = { viewModel.retryTask(task.id) },
                            onDeleteClick = { viewModel.deleteTask(task.id) }
                        )
                    }
                }

                if (taskGroups.pendingProcessing.isEmpty() &&
                    taskGroups.completed.isEmpty() &&
                    taskGroups.failed.isEmpty()
                ) {
                    item {
                        Text(
                            text = "Нет задач. Нажмите кнопку '+' чтобы добавить тестовое изображение.",
                            modifier = Modifier.padding(16.dp)
                        )
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
        }
    }
}

@Composable
fun PendingTaskItem(task: RecognitionTask) {
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
            Text("Задача #${task.id}")
            Text("Статус: ${task.status.name}", style = MaterialTheme.typography.bodySmall)
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
        onClick = onEditClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Задача #${task.id}")
                Text(
                    "Создана: ${formatDate(task.createdAt)}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text("Распознано", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onEditClick) {
                Icon(Icons.Default.Edit, contentDescription = "Редактировать")
            }
        }
    }
}

@Composable
fun FailedTaskItem(task: RecognitionTask, onRetryClick: () -> Unit, onDeleteClick: () -> Unit) {
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
            Text("Задача #${task.id}")
            Text(
                "Ошибка: ${task.errorMessage ?: "Неизвестная ошибка"}",
                style = MaterialTheme.typography.bodySmall
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onRetryClick) {
                    Icon(Icons.Default.Refresh, contentDescription = "Повторить")
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Default.Delete, contentDescription = "Удалить")
                }
            }
        }
    }
}

private fun formatDate(date: java.util.Date): String {
    val format = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    return format.format(date)
}