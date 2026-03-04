package com.chumakov123.casedocket.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
    viewModel: DraftListViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val taskGroups by viewModel.taskGroups.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Черновики") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.addTestImage(context) }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить тестовое изображение")
            }
        }
    ) { paddingValues ->
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

            // Пустое состояние
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
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
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