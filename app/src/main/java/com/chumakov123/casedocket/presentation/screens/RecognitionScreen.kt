package com.chumakov123.casedocket.presentation.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.chumakov123.casedocket.R
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
                actions = {
                    // Кнопка-заглушка настроек (три точки)
                    IconButton(
                        onClick = {
                            // TODO: открыть экран настроек
                            // Например: context.startActivity(Intent(context, SettingsActivity::class.java))
                            Log.d("MainScreen", "Settings clicked")
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.settings)
                        )
                    }
                }
            )
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
                        ScheduleHeader(schedule = state.schedule)
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

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.resetState() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.RestartAlt, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.new_test_recognition))
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

