package com.chumakov123.casedocket.presentation.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chumakov123.casedocket.R
import com.chumakov123.casedocket.presentation.viewmodel.OcrState

@Composable
fun ProcessingStatus(state: OcrState, processingTime: Long) {
    if (state !is OcrState.Idle) {
        Card(
            modifier = Modifier.Companion.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = when (state) {
                    is OcrState.Loading -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    is OcrState.Success -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                    is OcrState.Error -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                }
            )
        ) {
            Row(
                modifier = Modifier.Companion.padding(12.dp),
                verticalAlignment = Alignment.Companion.CenterVertically
            ) {
                Column {
                    Text(
                        text = when (state) {
                            is OcrState.Loading -> stringResource(R.string.processing)
                            is OcrState.Success -> stringResource(R.string.success)
                            is OcrState.Error -> stringResource(R.string.error)
                            else -> ""
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Companion.Medium
                    )
                    if (processingTime > 0) {
                        Text(
                            text = stringResource(R.string.processing_time, processingTime),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}