package com.chumakov123.casedocket.presentation.screens.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.chumakov123.casedocket.R
import com.chumakov123.casedocket.domain.model.court.draft.CourtCaseDraft
import com.chumakov123.casedocket.domain.model.court.toHHMM
import com.chumakov123.casedocket.domain.model.validation.CaseValidation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourtCaseCard(
    courtCaseDraft: CourtCaseDraft,
    validation: CaseValidation,
    onCaseNumberChange: (String) -> Unit,
    onTimeChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onPreliminaryChange: (Boolean) -> Unit,
    onVideoConferenceChange: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val caseNumber by remember(courtCaseDraft) {
        derivedStateOf { courtCaseDraft.caseNumber ?: "" }
    }
    val time by remember(courtCaseDraft) {
        derivedStateOf { courtCaseDraft.time?.toHHMM() ?: "" }
    }
    val description by remember(courtCaseDraft) {
        derivedStateOf { courtCaseDraft.description.text }
    }

    var editNumber by remember { mutableStateOf(false) }
    var editTime by remember { mutableStateOf(false) }
    var editDescription by remember { mutableStateOf(false) }

    var showDeleteCaseDialog by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                EditableTextBlock(
                    value = caseNumber,
                    placeholder = stringResource(R.string.case_number_placeholder),
                    textStyle = MaterialTheme.typography.titleMedium,
                    textColor = MaterialTheme.colorScheme.primary,
                    minLines = 1,
                    maxLines = 2,
                    isError = validation.caseNumberError,
                    modifier = Modifier.weight(1f)
                ) {
                    editNumber = true
                }

                Spacer(modifier = Modifier.width(8.dp))

                EditableTimeBlock(
                    time = time,
                    placeholder = stringResource(R.string.time_placeholder),
                    isError = validation.timeError,
                    modifier = Modifier
                ) {
                    editTime = true
                }

                IconButton(
                    onClick = { showDeleteCaseDialog = true },
                ) {
                    Icon(
                        Icons.Default.DeleteOutline,
                        contentDescription = stringResource(R.string.delete_case),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            EditableTextBlock(
                value = description,
                placeholder = stringResource(R.string.description_placeholder),
                minLines = 2,
                maxLines = Int.MAX_VALUE,
                isError = validation.descriptionError,
                modifier = Modifier.fillMaxWidth()
            ) {
                editDescription = true
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = courtCaseDraft.isPreliminary,
                    onClick = { onPreliminaryChange(!courtCaseDraft.isPreliminary) },
                    label = { Text(stringResource(R.string.psz)) },
                    leadingIcon = if (courtCaseDraft.isPreliminary) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                            )
                        }
                    } else null
                )

                FilterChip(
                    selected = courtCaseDraft.isVideoConference,
                    onClick = { onVideoConferenceChange(!courtCaseDraft.isVideoConference) },
                    label = { Text(stringResource(R.string.vks)) },
                    leadingIcon = if (courtCaseDraft.isVideoConference) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                            )
                        }
                    } else null
                )
            }
        }
    }

    if (editNumber) {
        EditTextDialog(
            title = stringResource(R.string.edit_case_number_title),
            initial = caseNumber,
            multiline = true,
            onDismiss = { editNumber = false },
            onConfirm = {
                onCaseNumberChange(it)
                editNumber = false
            }
        )
    }

    if (editTime) {
        TimePickerDialog(
            initialTime = time,
            onDismiss = { editTime = false },
            onConfirm = {
                onTimeChange(it)
                editTime = false
            }
        )
    }

    if (editDescription) {
        EditTextDialog(
            title = stringResource(R.string.edit_description_title),
            initial = description,
            multiline = true,
            onDismiss = { editDescription = false },
            onConfirm = {
                onDescriptionChange(it)
                editDescription = false
            }
        )
    }

    DeleteConfirmationDialog(
        showDialog = showDeleteCaseDialog,
        onDismiss = { showDeleteCaseDialog = false },
        onConfirm = {
            showDeleteCaseDialog = false
            onDelete()
        },
        title = stringResource(R.string.delete_case_confirmation_title),
        message = stringResource(R.string.delete_case_confirmation_message)
    )
}
