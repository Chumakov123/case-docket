package com.chumakov123.casedocket.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chumakov123.casedocket.domain.model.court.CourtSchedule
import com.chumakov123.casedocket.presentation.viewmodel.ConfirmedListViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun ConfirmedListScreen(
    onEditClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ConfirmedListViewModel = koinViewModel()
) {
    val schedules by viewModel.schedules.collectAsState()

    if (schedules.isEmpty()) {
        Text(
            text = "Нет подтверждённых расписаний",
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            style = MaterialTheme.typography.bodyLarge
        )
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(schedules) { schedule ->
                ConfirmedScheduleCard(
                    schedule = schedule,
                    onEditClick = { onEditClick(schedule.id) }
                )
            }
        }
    }
}

@Composable
fun ConfirmedScheduleCard(
    schedule: CourtSchedule,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = schedule.date.toDisplayFormat(),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = schedule.judge.text,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Дел: ${schedule.cases.size}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            IconButton(onClick = onEditClick) {
                Icon(Icons.Default.Edit, contentDescription = "Редактировать")
            }
        }
    }
}