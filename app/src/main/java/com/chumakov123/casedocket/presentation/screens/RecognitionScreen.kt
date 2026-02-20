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
            when (ocrState) {
                is OcrState.Success -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 4.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SmallFloatingActionButton(
                            onClick = { viewModel.resetState() },
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.cancel)
                            )
                        }

                        FloatingActionButton(
                            onClick = {
                                Log.d("RecognitionScreen", "Confirm recognition clicked")
                            },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null
                            )
                        }
                    }
                }
                else -> { }
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
                    item {
                        ScheduleHeader(schedule = state.schedule, onDateChange = { /* TODO onDateChange */ })
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            stringResource(R.string.recognized_cases_title),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    items(state.schedule.cases) { courtCase ->
                        CourtCaseCard(courtCaseDraft = courtCase)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}