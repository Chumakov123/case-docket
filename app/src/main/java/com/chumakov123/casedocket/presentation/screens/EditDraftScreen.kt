package com.chumakov123.casedocket.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.chumakov123.casedocket.R
import com.chumakov123.casedocket.presentation.screens.components.AddCaseButton
import com.chumakov123.casedocket.presentation.screens.components.CourtCaseCard
import com.chumakov123.casedocket.presentation.screens.components.DeleteConfirmationDialog
import com.chumakov123.casedocket.presentation.screens.components.ErrorState
import com.chumakov123.casedocket.presentation.screens.components.LoadingState
import com.chumakov123.casedocket.presentation.screens.components.ScheduleHeader
import com.chumakov123.casedocket.presentation.viewmodel.EditDraftState
import com.chumakov123.casedocket.presentation.viewmodel.EditDraftViewModel
import com.chumakov123.casedocket.presentation.viewmodel.EditMode
import com.chumakov123.casedocket.util.toDisplayString
import my.nanihadesuka.compose.LazyColumnScrollbar
import my.nanihadesuka.compose.ScrollbarSettings
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDraftScreen(
    taskId: Long?,
    confirmedId: Long?,
    onNavigateBack: () -> Unit,
    viewModel: EditDraftViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val currentDraft by viewModel.currentDraft.collectAsState()
    val validation by viewModel.validation.collectAsState()
    val mode by viewModel.mode.collectAsState()

    val errorCount = remember(validation) {
        var count = 0
        if (validation.dateError) count++
        if (validation.judgeError) count++
        if (validation.casesError) count++
        validation.casesValidations.forEach {
            if (it.caseNumberError) count++
            if (it.timeError) count++
            if (it.descriptionError) count++
        }
        count
    }

    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    LaunchedEffect(taskId, confirmedId) {
        when {
            taskId != null -> viewModel.setMode(EditMode.Draft(taskId))
            confirmedId != null -> viewModel.setMode(EditMode.Confirmed(confirmedId))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            when (mode) {
                                is EditMode.Draft -> stringResource(R.string.edit_draft_title)
                                is EditMode.Confirmed -> stringResource(R.string.edit_confirmed_title)
                                null -> stringResource(R.string.loading)
                            }
                        )
                        if (errorCount > 0) {
                            Text(
                                text = stringResource(R.string.errors_remaining, errorCount),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
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
                    when (mode) {
                        is EditMode.Draft -> {
                            SmallFloatingActionButton(
                                onClick = {
                                    showDeleteConfirmationDialog = true
                                },
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ) {
                                Icon(
                                    Icons.Default.DeleteForever,
                                    contentDescription = stringResource(R.string.reject)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            FloatingActionButton(
                                onClick = {
                                    if (validation.isValid) {
                                        viewModel.confirmDraft(onNavigateBack)
                                    }
                                },
                                containerColor = if (validation.isValid)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (validation.isValid)
                                    MaterialTheme.colorScheme.onPrimary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = stringResource(R.string.confirm)
                                )
                            }
                        }

                        is EditMode.Confirmed -> {
                            SmallFloatingActionButton(
                                onClick = {
                                    showDeleteConfirmationDialog = true
                                },
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ) {
                                Icon(
                                    Icons.Default.DeleteForever,
                                    contentDescription = stringResource(R.string.cancel)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            FloatingActionButton(
                                onClick = {
                                    if (validation.isValid) {
                                        viewModel.saveConfirmed(onNavigateBack)
                                    }
                                },
                                containerColor = if (validation.isValid)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (validation.isValid)
                                    MaterialTheme.colorScheme.onPrimary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = stringResource(R.string.save)
                                )
                            }
                        }

                        null -> {}
                    }
                }
            }
        }
    ) { paddingValues ->
        when (val currentState = state) {
            is EditDraftState.Idle -> Unit
            is EditDraftState.Loading -> LoadingState()
            is EditDraftState.Error -> {
                val currentMode = mode
                ErrorState(
                    message = currentState.type.toDisplayString(),
                    onRetry = {
                        when (currentMode) {
                            is EditMode.Draft -> viewModel.setMode(currentMode)
                            is EditMode.Confirmed -> viewModel.setMode(currentMode)
                            null -> {}
                        }
                    },
                )
            }

            is EditDraftState.Success -> {
                if (currentDraft != null) {
                    Box(modifier = Modifier.padding(paddingValues)) {
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
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(bottom = 80.dp)
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
                                        onCaseNumberChange = {
                                            viewModel.updateCaseNumber(
                                                index,
                                                it
                                            )
                                        },
                                        onTimeChange = { viewModel.updateCaseTime(index, it) },
                                        onDescriptionChange = {
                                            viewModel.updateCaseDescription(
                                                index,
                                                it
                                            )
                                        },
                                        onPreliminaryChange = {
                                            viewModel.updateCasePreliminary(
                                                index,
                                                it
                                            )
                                        },
                                        onVideoConferenceChange = {
                                            viewModel.updateCaseVideoConference(
                                                index,
                                                it
                                            )
                                        },
                                        onDelete = { viewModel.deleteCase(index) }
                                    )
                                }
                                if (validation.casesError) {
                                    item {
                                        Text(
                                            stringResource(R.string.cases_list_empty),
                                            color = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.padding(16.dp)
                                        )
                                    }
                                }
                                item {
                                    AddCaseButton(onClick = { viewModel.addCase() })
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    DeleteConfirmationDialog(
        showDialog = showDeleteConfirmationDialog,
        onDismiss = { showDeleteConfirmationDialog = false },
        onConfirm = {
            showDeleteConfirmationDialog = false
            when (mode) {
                is EditMode.Draft -> viewModel.rejectDraft(onNavigateBack)
                is EditMode.Confirmed -> viewModel.deleteConfirmed(onNavigateBack)
                null -> {}
            }
        }
    )
}