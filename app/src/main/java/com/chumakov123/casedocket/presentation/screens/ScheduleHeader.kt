package com.chumakov123.casedocket.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.chumakov123.casedocket.R
import com.chumakov123.casedocket.domain.model.court.draft.CourtScheduleDraft

@Composable
fun ScheduleHeader(schedule: CourtScheduleDraft) {
    var dateText by remember { mutableStateOf(schedule.date?.toDisplayFormat() ?: "") }
    var judgeText by remember { mutableStateOf(schedule.judge.text) }
    val casesCount = schedule.cases.size

    Card(
        modifier = Modifier.Companion.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.Companion.padding(16.dp)) {
            Row(
                modifier = Modifier.Companion.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Companion.CenterVertically
            ) {
                Column {
                    Text(
                        stringResource(R.string.schedule_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Companion.Bold
                    )
                    Text(
                        stringResource(R.string.schedule_instruction),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    Icons.Outlined.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.Companion.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.Companion.height(12.dp))

            OutlinedTextField(
                value = dateText,
                onValueChange = { dateText = it },
                label = { Text(stringResource(R.string.date_label)) },
                placeholder = { Text(stringResource(R.string.date_placeholder)) },
                leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.Companion
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Companion.Text)
            )

            OutlinedTextField(
                value = judgeText,
                onValueChange = { judgeText = it },
                label = { Text(stringResource(R.string.judge_label)) },
                placeholder = { Text(stringResource(R.string.judge_placeholder)) },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.Companion
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Companion.Text)
            )

            Spacer(modifier = Modifier.Companion.height(8.dp))

            Row(verticalAlignment = Alignment.Companion.CenterVertically) {
                Icon(
                    Icons.AutoMirrored.Filled.List,
                    contentDescription = null,
                    modifier = Modifier.Companion.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.Companion.width(8.dp))
                Text(
                    stringResource(R.string.total_cases, casesCount),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}