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
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.chumakov123.casedocket.presentation.screens.components.CourtCaseCard
import com.chumakov123.casedocket.presentation.screens.components.ErrorState
import com.chumakov123.casedocket.presentation.screens.components.LoadingState
import com.chumakov123.casedocket.presentation.screens.components.ScheduleHeader
import com.chumakov123.casedocket.presentation.viewmodel.EditDraftState
import com.chumakov123.casedocket.presentation.viewmodel.EditDraftViewModel
import com.chumakov123.casedocket.presentation.viewmodel.EditMode
import com.chumakov123.casedocket.util.toDisplayString
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

    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }

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
                    Text(
                        when (mode) {
                            is EditMode.Draft -> stringResource(R.string.edit_draft_title)
                            is EditMode.Confirmed -> stringResource(R.string.edit_confirmed_title)
                            null -> stringResource(R.string.loading)
                        }
                    )
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
                            if (validation.isValid) {
                                FloatingActionButton(
                                    onClick = { viewModel.confirmDraft(onNavigateBack) },
                                    containerColor = MaterialTheme.colorScheme.primary
                                ) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = stringResource(R.string.confirm)
                                    )
                                }
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
                            if (validation.isValid) {
                                FloatingActionButton(
                                    onClick = { viewModel.saveConfirmed(onNavigateBack) },
                                    containerColor = MaterialTheme.colorScheme.primary
                                ) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = stringResource(R.string.save)
                                    )
                                }
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
                                    stringResource(R.string.cases_list_empty),
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

    if (showDeleteConfirmationDialog) {
        AlertDialog(
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(
                    text = stringResource(R.string.delete_confirmation_title),
                    color = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.delete_confirmation_message)
                )
            },
            onDismissRequest = {
                showDeleteConfirmationDialog = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmationDialog = false
                        when (mode) {
                            is EditMode.Draft -> viewModel.rejectDraft(onNavigateBack)
                            is EditMode.Confirmed -> viewModel.deleteConfirmed(onNavigateBack)
                            null -> {}
                        }
                    },
                    content = {
                        Text(
                            stringResource(R.string.delete),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                )
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmationDialog = false
                    }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}