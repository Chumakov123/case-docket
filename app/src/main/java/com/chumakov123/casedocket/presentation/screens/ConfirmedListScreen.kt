package com.chumakov123.casedocket.presentation.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.chumakov123.casedocket.R
import com.chumakov123.casedocket.domain.model.court.CaseResult
import com.chumakov123.casedocket.domain.model.court.CaseTime
import com.chumakov123.casedocket.domain.model.court.CourtCase
import com.chumakov123.casedocket.domain.model.court.CourtCaseDescription
import com.chumakov123.casedocket.domain.model.court.CourtSchedule
import com.chumakov123.casedocket.domain.model.court.Judge
import com.chumakov123.casedocket.domain.model.court.ScheduleDate
import com.chumakov123.casedocket.domain.model.court.toHHMM
import com.chumakov123.casedocket.presentation.screens.components.EmptyState
import com.chumakov123.casedocket.presentation.theme.AppTheme
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
                    EmptyState(
                        message = stringResource(R.string.no_actual_schedule),
                        icon = Icons.AutoMirrored.Filled.EventNote
                    )
                }
            }

            activeItems.isEmpty() && !showArchived -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyState(
                        message = stringResource(R.string.no_actual_schedule),
                        icon = Icons.AutoMirrored.Filled.EventNote
                    )
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
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = schedule.date.toDisplayFormat(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = schedule.judge.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.cases_count, schedule.cases.size),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(
                onClick = onEditClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = stringResource(R.string.edit_schedule),
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(20.dp)
                )
            }
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
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        ),
        shape = MaterialTheme.shapes.medium,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = case.caseNumber,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = case.time.toHHMM(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (case.isPreliminary || case.isVideoConference) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (case.isPreliminary) {
                        StatusTag(
                            text = stringResource(R.string.psz),
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }

                    if (case.isVideoConference) {
                        StatusTag(
                            text = stringResource(R.string.vks),
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = case.description.text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                ResultSelector(
                    selectedResult = case.result,
                    onResultSelected = onResultSelected
                )
            }
        }
    }
}

@Composable
private fun StatusTag(
    text: String,
    containerColor: Color,
    contentColor: Color
) {
    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = MaterialTheme.shapes.extraSmall
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun ResultSelector(
    selectedResult: CaseResult?,
    onResultSelected: (CaseResult?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        AssistChip(
            onClick = { expanded = true },
            label = {
                Text(
                    text = selectedResult?.let { stringResource(getResultStringRes(it)) }
                        ?: stringResource(R.string.select_result),
                    style = MaterialTheme.typography.labelMedium
                )
            },
            colors = if (selectedResult != null) {
                AssistChipDefaults.assistChipColors(
                    labelColor = MaterialTheme.colorScheme.primary,
                    leadingIconContentColor = MaterialTheme.colorScheme.primary,
                    trailingIconContentColor = MaterialTheme.colorScheme.primary
                )
            } else {
                AssistChipDefaults.assistChipColors()
            }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            CaseResult.entries.forEach { result ->
                DropdownMenuItem(
                    text = { Text(stringResource(getResultStringRes(result))) },
                    onClick = {
                        onResultSelected(result)
                        expanded = false
                    }
                )
            }
            if (selectedResult != null) {
                HorizontalDivider()
                DropdownMenuItem(
                    text = {
                        Text(
                            stringResource(R.string.clear_result),
                            color = MaterialTheme.colorScheme.error
                        )
                    },
                    onClick = {
                        onResultSelected(null)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CaseItemPreview() {
    AppTheme {
        Box(Modifier.padding(16.dp)) {
            CaseItem(
                case = CourtCase(
                    caseNumber = "А40-123456/2023",
                    time = CaseTime(10, 30),
                    description = CourtCaseDescription("Истец: ООО 'Ромашка', Ответчик: ПАО 'Сбербанк'"),
                    isPreliminary = true,
                    isVideoConference = true,
                    result = CaseResult.ADJOURNMENT
                ),
                isPast = false,
                onResultSelected = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ScheduleHeaderItemPreview() {
    AppTheme {
        ScheduleHeaderItem(
            schedule = CourtSchedule(
                date = ScheduleDate.parse("15.05.2024")!!,
                judge = Judge("Сидоров С.С."),
                cases = listOf(
                    CourtCase("1", CaseTime(9, 0), CourtCaseDescription("Test"))
                )
            ),
            onEditClick = {}
        )
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