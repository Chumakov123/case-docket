package com.chumakov123.casedocket.presentation.screens.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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

@Composable
fun CourtCaseCard(
    courtCaseDraft: CourtCaseDraft,
    validation: CaseValidation,
    onCaseNumberChange: (String) -> Unit,
    onTimeChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit
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

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(14.dp)) {

            Box(modifier = Modifier.fillMaxWidth()) {
                EditableTextBlock(
                    value = caseNumber,
                    placeholder = stringResource(R.string.case_number_placeholder),
                    textStyle = MaterialTheme.typography.titleMedium,
                    textColor = MaterialTheme.colorScheme.primary,
                    minLines = 1,
                    maxLines = 2,
                    isError = validation.caseNumberError,
                    modifier = Modifier
                        .fillMaxWidth(0.55f)
                        .align(Alignment.TopStart)
                ) {
                    editNumber = true
                }

                EditableTimeBlock(
                    time = time,
                    placeholder = stringResource(R.string.time_placeholder),
                    isError = validation.timeError,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    editTime = true
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
}
