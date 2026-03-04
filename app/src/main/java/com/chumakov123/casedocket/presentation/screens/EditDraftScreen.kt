package com.chumakov123.casedocket.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.chumakov123.casedocket.R
import com.chumakov123.casedocket.presentation.screens.components.CourtCaseCard
import com.chumakov123.casedocket.presentation.screens.components.ErrorState
import com.chumakov123.casedocket.presentation.screens.components.LoadingState
import com.chumakov123.casedocket.presentation.screens.components.ScheduleHeader
import com.chumakov123.casedocket.presentation.viewmodel.EditDraftState
import com.chumakov123.casedocket.presentation.viewmodel.EditDraftViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDraftScreen(
    taskId: Long,
    onNavigateBack: () -> Unit,
    viewModel: EditDraftViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val currentDraft by viewModel.currentDraft.collectAsState()
    val validation by viewModel.validation.collectAsState()

    LaunchedEffect(taskId) {
        viewModel.setTaskId(taskId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Редактирование черновика") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (state is EditDraftState.Success) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 4.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    SmallFloatingActionButton(
                        onClick = { viewModel.rejectDraft(onNavigateBack) },
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Отклонить")
                    }
                    if (validation.isValid) {
                        FloatingActionButton(
                            onClick = { viewModel.confirmDraft(onNavigateBack) },
                            containerColor = MaterialTheme.colorScheme.primary
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Подтвердить")
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        when (val currentState = state) {
            is EditDraftState.Idle -> Unit
            is EditDraftState.Loading -> LoadingState()
            is EditDraftState.Error -> ErrorState(
                message = currentState.message,
                onRetry = { viewModel.setTaskId(taskId) },
            )

            is EditDraftState.Success -> {
                if (currentDraft != null) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
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
                                onCaseNumberChange = { viewModel.updateCaseNumber(index, it) },
                                onTimeChange = { viewModel.updateCaseTime(index, it) },
                                onDescriptionChange = { viewModel.updateCaseDescription(index, it) }
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
        }
    }
}