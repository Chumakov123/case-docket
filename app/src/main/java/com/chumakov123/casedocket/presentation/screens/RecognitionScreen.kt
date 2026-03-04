package com.chumakov123.casedocket.presentation.screens

import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.chumakov123.casedocket.R
import com.chumakov123.casedocket.domain.model.RecognitionTask
import com.chumakov123.casedocket.domain.model.TaskStatus
import com.chumakov123.casedocket.presentation.screens.components.CourtCaseCard
import com.chumakov123.casedocket.presentation.screens.components.ErrorState
import com.chumakov123.casedocket.presentation.screens.components.IdleState
import com.chumakov123.casedocket.presentation.screens.components.LoadingState
import com.chumakov123.casedocket.presentation.screens.components.ProcessingStatus
import com.chumakov123.casedocket.presentation.screens.components.ScheduleHeader
import com.chumakov123.casedocket.presentation.viewmodel.OcrState
import com.chumakov123.casedocket.presentation.viewmodel.OcrViewModel
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecognitionScreen(viewModel: OcrViewModel = koinViewModel()) {
    val context = LocalContext.current
    val ocrState by viewModel.ocrState.collectAsState()
    val currentDraft by viewModel.currentDraft.collectAsState()
    val validation by viewModel.validation.collectAsState()
    val processingTime by viewModel.processingTime.collectAsState()
    val tasks by viewModel.tasks.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                navigationIcon = {
                    IconButton(
                        onClick = {
                            // TODO: реализовать навигацию назад
                            Log.d("RecognitionScreen", "Navigate back clicked")
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            // TODO: открыть экран настроек
                            Log.d("RecognitionScreen", "Settings clicked")
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.settings)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (ocrState is OcrState.Success) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 4.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    SmallFloatingActionButton(
                        onClick = { viewModel.resetState() },
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Отмена")
                    }
                    if (validation.isValid) {
                        FloatingActionButton(
                            onClick = { viewModel.confirmSchedule() },
                            containerColor = MaterialTheme.colorScheme.primary
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                ProcessingStatus(ocrState, processingTime)
            }

            item {
                Button(
                    onClick = { viewModel.submitTestImage(context, "test_schedule.jpg") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Добавить тестовое изображение в очередь")
                }
            }

            if (tasks.isNotEmpty()) {
                item {
                    TasksSection(tasks = tasks)
                }
            }

            item {
                ProcessingStatus(ocrState, processingTime)
            }

            when (val state = ocrState) {
                is OcrState.Idle -> {
                    item {
                        IdleState(onTestClick = {
                            viewModel.recognizeScheduleFromAssets(context, "test_schedule.jpg")
                        })
                    }
                }

                is OcrState.Loading -> {
                    item { LoadingState() }
                }

                is OcrState.Error -> {
                    item {
                        ErrorState(
                            message = state.message,
                            onRetry = { viewModel.resetState() }
                        )
                    }
                }

                is OcrState.Success -> {
                    if (currentDraft != null) {
                        item {
                            ScheduleHeader(
                                schedule = currentDraft!!,
                                dateError = validation.dateError,
                                judgeError = validation.judgeError,
                                onDateChange = { viewModel.updateDate(it) },
                                onJudgeChange = { viewModel.updateJudge(it) }
                            )
                        }

                        items(currentDraft!!.cases.indices.toList()) { index ->
                            CourtCaseCard(
                                courtCaseDraft = currentDraft!!.cases[index],
                                validation = validation.casesValidations[index],
                                onCaseNumberChange = { newNumber ->
                                    viewModel.updateCaseNumber(index, newNumber)
                                },
                                onTimeChange = { newTime ->
                                    viewModel.updateCaseTime(index, newTime)
                                },
                                onDescriptionChange = { newDesc ->
                                    viewModel.updateCaseDescription(index, newDesc)
                                }
                            )
                        }

                        if (validation.casesError) {
                            item {
                                Text(
                                    "Список дел пуст",
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun TaskItem(task: RecognitionTask, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (task.status) {
                TaskStatus.PENDING -> MaterialTheme.colorScheme.secondaryContainer
                TaskStatus.PROCESSING -> MaterialTheme.colorScheme.tertiaryContainer
                TaskStatus.COMPLETED -> MaterialTheme.colorScheme.primaryContainer
                TaskStatus.FAILED -> MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("ID: ${task.id}")
                Text("Статус: ${task.status.name}")
                Text("Создана: ${formatDate(task.createdAt)}")
                if (task.errorMessage != null) {
                    Text("Ошибка: ${task.errorMessage}", color = MaterialTheme.colorScheme.error)
                }
            }
            if (task.status == TaskStatus.COMPLETED) {
                Icon(Icons.Default.Check, contentDescription = null)
            }
        }
    }
}

@Composable
fun TasksSection(tasks: List<RecognitionTask>) {
    var expanded by remember { mutableStateOf(true) }

    val totalCount = tasks.size
    val completedCount = tasks.count { it.status == TaskStatus.COMPLETED }
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.animateContentSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Очередь задач",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "$completedCount из $totalCount выполнено",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Icon(
                    imageVector = Icons.Default.ExpandLess,
                    contentDescription = "Свернуть"
                )
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(4.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
            )

            if (expanded) {
                tasks.forEach { task ->
                    TaskItem(
                        task = task,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

fun formatDate(date: Date): String {
    val format = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    return format.format(date)
}