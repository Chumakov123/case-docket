package com.chumakov123.casedocket.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.chumakov123.casedocket.R
import com.chumakov123.casedocket.domain.model.court.CaseResult
import com.chumakov123.casedocket.domain.model.court.CourtCase
import com.chumakov123.casedocket.domain.model.court.CourtSchedule
import com.chumakov123.casedocket.domain.model.court.toHHMM
import com.chumakov123.casedocket.presentation.screens.components.EmptyState
import com.chumakov123.casedocket.presentation.viewmodel.ConfirmedListItem
import com.chumakov123.casedocket.presentation.viewmodel.ConfirmedListViewModel
import my.nanihadesuka.compose.LazyColumnScrollbar
import my.nanihadesuka.compose.ScrollbarSettings
import org.koin.androidx.compose.koinViewModel

@Composable
fun ConfirmedListScreen(
    onEditClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ConfirmedListViewModel = koinViewModel()
) {
    val activeItems by viewModel.activeItems.collectAsState()
    val archivedItems by viewModel.archivedItems.collectAsState()
    val hasArchived by viewModel.hasArchived.collectAsState()
    val showArchived by viewModel.showArchived.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    val listState = rememberLazyListState()

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshTime()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        if (hasArchived) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                ArchiveButton(
                    showArchived = showArchived,
                    onClick = { viewModel.toggleShowArchived() }
                )
            }
        }

        when {
            activeItems.isEmpty() && archivedItems.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyState(message = stringResource(R.string.no_actual_schedule))
                }
            }

            activeItems.isEmpty() && !showArchived -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyState(message = stringResource(R.string.no_actual_schedule))
                }
            }

            else -> {
                LazyColumnScrollbar(
                    state = listState,
                    settings = ScrollbarSettings.Default.copy(
                        thumbUnselectedColor = Color.Gray.copy(alpha = 0.5f),
                        thumbSelectedColor = MaterialTheme.colorScheme.primary,
                        scrollbarPadding = 4.dp
                    )
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        if (showArchived) {
                            items(archivedItems) { item ->
                                when (item) {
                                    is ConfirmedListItem.Header -> ScheduleHeaderItem(
                                        schedule = item.schedule,
                                        onEditClick = { onEditClick(item.schedule.id) }
                                    )

                                    is ConfirmedListItem.Case -> CaseItem(
                                        case = item.case,
                                        isPast = item.isPast,
                                        onResultSelected = { result ->
                                            viewModel.updateCaseResult(
                                                item.scheduleId,
                                                item.case.caseNumber,
                                                result
                                            )
                                        }
                                    )
                                }
                            }
                        }

                        items(activeItems) { item ->
                            when (item) {
                                is ConfirmedListItem.Header -> ScheduleHeaderItem(
                                    schedule = item.schedule,
                                    onEditClick = { onEditClick(item.schedule.id) }
                                )

                                is ConfirmedListItem.Case -> CaseItem(
                                    case = item.case,
                                    isPast = item.isPast,
                                    onResultSelected = { result ->
                                        viewModel.updateCaseResult(
                                            item.scheduleId,
                                            item.case.caseNumber,
                                            result
                                        )
                                    }
                                )
                            }
                        }
                    }
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
                text = stringResource(R.string.cases_count, schedule.cases.size),
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
                contentDescription = stringResource(R.string.edit_schedule),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun CaseItem(
    case: CourtCase,
    isPast: Boolean,
    onResultSelected: (CaseResult?) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (isPast) 0.5f else 1f),
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
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )

                if (case.isPreliminary) {
                    Text(
                        text = stringResource(R.string.psz),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.tertiaryContainer,
                                shape = MaterialTheme.shapes.extraSmall
                            )
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                if (case.isVideoConference) {
                    Text(
                        text = stringResource(R.string.vks),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = MaterialTheme.shapes.extraSmall
                            )
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Text(
                    text = case.time.toHHMM(),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = case.description.text,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            ResultSelector(
                selectedResult = case.result,
                onResultSelected = onResultSelected
            )
        }
    }
}

@Composable
fun ResultSelector(
    selectedResult: CaseResult?,
    onResultSelected: (CaseResult?) -> Unit
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CaseResult.entries.forEach { result ->
            val isSelected = selectedResult == result
            SuggestionChip(
                onClick = { onResultSelected(if (isSelected) null else result) },
                label = {
                    Text(
                        text = stringResource(getResultStringRes(result)),
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = if (isSelected) {
                    SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                } else {
                    SuggestionChipDefaults.suggestionChipColors()
                },
                border = if (isSelected) null else SuggestionChipDefaults.suggestionChipBorder(true)
            )
        }
    }
}

private fun getResultStringRes(result: CaseResult): Int = when (result) {
    CaseResult.RECESS -> R.string.result_recess
    CaseResult.ADJOURNMENT -> R.string.result_adjournment
    CaseResult.EXPERTISE -> R.string.result_expertise
    CaseResult.RESTARTED -> R.string.result_restarted
    CaseResult.DECISION -> R.string.result_decision
}

@Composable
fun ArchiveButton(showArchived: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.small
    ) {
        Icon(
            imageVector = if (showArchived) Icons.Default.VisibilityOff else Icons.Default.Visibility,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = if (showArchived)
                stringResource(R.string.hide_past)
            else
                stringResource(R.string.show_past)
        )
    }
}