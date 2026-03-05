package com.chumakov123.casedocket.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.chumakov123.casedocket.domain.model.court.CourtCase
import com.chumakov123.casedocket.domain.model.court.CourtSchedule
import com.chumakov123.casedocket.domain.model.court.toHHMM
import com.chumakov123.casedocket.presentation.viewmodel.ConfirmedListItem
import com.chumakov123.casedocket.presentation.viewmodel.ConfirmedListViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun ConfirmedListScreen(
    onEditClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ConfirmedListViewModel = koinViewModel()
) {
    val items by viewModel.items.collectAsState()

    if (items.isEmpty()) {
        Text(
            text = "Нет подтверждённых расписаний",
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            style = MaterialTheme.typography.bodyLarge
        )
    } else {
        LazyColumn(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(items) { item ->
                when (item) {
                    is ConfirmedListItem.Header -> ScheduleHeaderItem(
                        schedule = item.schedule,
                        onEditClick = { onEditClick(item.schedule.id) }
                    )

                    is ConfirmedListItem.Case -> CaseItem(
                        case = item.case,
                    )
                }
            }
        }
    }
}

@Composable
fun ScheduleHeaderItem(schedule: CourtSchedule, onEditClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
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
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(
            onClick = onEditClick,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                Icons.Default.Edit,
                contentDescription = "Редактировать расписание",
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun CaseItem(case: CourtCase, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = case.caseNumber,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = case.time.toHHMM(),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(Modifier.height(4.dp))

            Text(
                text = case.description.text,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}