package com.chumakov123.casedocket.presentation.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.chumakov123.casedocket.R
import com.chumakov123.casedocket.presentation.screens.components.CourtCaseCard
import com.chumakov123.casedocket.presentation.screens.components.ErrorState
import com.chumakov123.casedocket.presentation.screens.components.IdleState
import com.chumakov123.casedocket.presentation.screens.components.LoadingState
import com.chumakov123.casedocket.presentation.screens.components.ProcessingStatus
import com.chumakov123.casedocket.presentation.screens.components.ScheduleHeader
import com.chumakov123.casedocket.presentation.viewmodel.OcrState
import com.chumakov123.casedocket.presentation.viewmodel.OcrViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecognitionScreen(viewModel: OcrViewModel = koinViewModel()) {
    val context = LocalContext.current
    val ocrState by viewModel.ocrState.collectAsState()
    val currentDraft by viewModel.currentDraft.collectAsState()
    val validation by viewModel.validation.collectAsState()
    val processingTime by viewModel.processingTime.collectAsState()

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