package com.chumakov123.casedocket.presentation.screens.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

@Composable
fun CourtCaseCard(courtCaseDraft: CourtCaseDraft) {
    var caseNumber by remember { mutableStateOf(courtCaseDraft.caseNumber ?: "") }
    var time by remember { mutableStateOf(courtCaseDraft.time?.toHHMM() ?: "") }
    var description by remember { mutableStateOf(courtCaseDraft.description.text) }

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
                    modifier = Modifier
                        .fillMaxWidth(0.55f)
                        .align(Alignment.TopStart)
                ) {
                    editNumber = true
                }

                Surface(
                    onClick = { editTime = true },
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Text(
                        text = time.ifBlank { stringResource(R.string.time_placeholder) },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            EditableTextBlock(
                value = description,
                placeholder = stringResource(R.string.description_placeholder),
                minLines = 2,
                maxLines = Int.MAX_VALUE,
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
                caseNumber = it
                editNumber = false
            }
        )
    }

    if (editTime) {
        TimePickerDialog(
            initialTime = time,
            onDismiss = { editTime = false },
            onConfirm = { newTime ->
                time = newTime
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
                description = it
                editDescription = false
            }
        )
    }
}

