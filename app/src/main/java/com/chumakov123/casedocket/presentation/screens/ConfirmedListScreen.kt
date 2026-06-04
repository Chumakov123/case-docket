package com.chumakov123.casedocket.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ConfirmedListScreen(
    onEditClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ConfirmedListViewModel = koinViewModel()
) {
    val activeItems by viewModel.activeItems.collectAsState()
    val archivedItems by viewModel.archivedItems.collectAsState()
    val showArchived by viewModel.showArchived.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    val listState = rememberLazyListState()

    // State for expanded/collapsed schedules
    val expandedStates = remember { mutableStateMapOf<Long, Boolean>() }

    // Logic for initial expansion
    LaunchedEffect(activeItems, archivedItems, showArchived) {
        val allSchedules = mutableListOf<CourtSchedule>()
        if (showArchived) {
            allSchedules.addAll(
                archivedItems.filterIsInstance<ConfirmedListItem.Header>()
                    .map { it.schedule })
        }
        allSchedules.addAll(
            activeItems.filterIsInstance<ConfirmedListItem.Header>()
                .map { it.schedule })

        if (allSchedules.size == 1) {
            expandedStates[allSchedules.first().id] = true
        } else if (expandedStates.isEmpty()) {
            // Find the first active schedule
            val firstActive = activeItems.filterIsInstance<ConfirmedListItem.Header>()
                .firstOrNull()?.schedule
            if (firstActive != null) {
                expandedStates[firstActive.id] = true
            }
        }
    }

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
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        if (showArchived) {
                            val groupedArchived = archivedItems.groupBy { item ->
                                when (item) {
                                    is ConfirmedListItem.Header -> item.schedule.id
                                    is ConfirmedListItem.Case -> item.scheduleId
                                }
                            }
                            groupedArchived.forEach { (scheduleId, items) ->
                                val header =
                                    items.filterIsInstance<ConfirmedListItem.Header>().first()
                                val cases = items.filterIsInstance<ConfirmedListItem.Case>()
                                val isExpanded = expandedStates[scheduleId] ?: false

                                stickyHeader(key = "archived_header_$scheduleId") {
                                    Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
                                        Spacer(Modifier.height(8.dp))
                                        ScheduleHeaderItem(
                                            schedule = header.schedule,
                                            isExpanded = isExpanded,
                                            onExpandClick = {
                                                expandedStates[scheduleId] = !isExpanded
                                            },
                                            onEditClick = { onEditClick(header.schedule.id) }
                                        )
                                    }
                                }

                                items(
                                    items = cases,
                                    key = { "archived_${scheduleId}_${it.case.caseNumber}" }
                                ) { item ->
                                    AnimatedVisibility(
                                        visible = isExpanded,
                                        enter = expandVertically() + fadeIn(),
                                        exit = shrinkVertically() + fadeOut()
                                    ) {
                                        Column {
                                            Spacer(Modifier.height(8.dp))
                                            CaseItem(
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

                        val groupedActive = activeItems.groupBy { item ->
                            when (item) {
                                is ConfirmedListItem.Header -> item.schedule.id
                                is ConfirmedListItem.Case -> item.scheduleId
                            }
                        }
                        groupedActive.forEach { (scheduleId, items) ->
                            val header = items.filterIsInstance<ConfirmedListItem.Header>().first()
                            val cases = items.filterIsInstance<ConfirmedListItem.Case>()
                            val isExpanded = expandedStates[scheduleId] ?: false

                            stickyHeader(key = "header_$scheduleId") {
                                Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
                                    Spacer(Modifier.height(8.dp))
                                    ScheduleHeaderItem(
                                        schedule = header.schedule,
                                        isExpanded = isExpanded,
                                        onExpandClick = {
                                            expandedStates[scheduleId] = !isExpanded
                                        },
                                        onEditClick = { onEditClick(header.schedule.id) }
                                    )
                                }
                            }

                            items(
                                items = cases,
                                key = { "${scheduleId}_${it.case.caseNumber}" }
                            ) { item ->
                                AnimatedVisibility(
                                    visible = isExpanded,
                                    enter = expandVertically() + fadeIn(),
                                    exit = shrinkVertically() + fadeOut()
                                ) {
                                    Column {
                                        Spacer(Modifier.height(8.dp))
                                        CaseItem(
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
    }
}

@Composable
fun ScheduleHeaderItem(
    schedule: CourtSchedule,
    isExpanded: Boolean,
    onExpandClick: () -> Unit,
    onEditClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onExpandClick),
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = schedule.date.toDisplayFormat(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
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
            }
            IconButton(
                onClick = onEditClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = stringResource(R.string.edit_schedule),
                    tint = MaterialTheme.colorScheme.primary,
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
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (isPast) 0.6f else 1f),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = if (isPast) 1.dp else 3.dp
        ),
        shape = MaterialTheme.shapes.medium
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
                            icon = Icons.Default.History,
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }

                    if (case.isVideoConference) {
                        StatusTag(
                            text = stringResource(R.string.vks),
                            icon = Icons.Default.Videocam,
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
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color
) {
    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = MaterialTheme.shapes.extraSmall
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall
            )
        }
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
            colors = selectedResult?.let { getResultColors(it) }
                ?: AssistChipDefaults.assistChipColors()
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
            isExpanded = true,
            onExpandClick = {},
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
private fun getResultColors(result: CaseResult) = when (val isDark = isSystemInDarkTheme()) {
    else -> when (result) {
        CaseResult.RECESS -> AssistChipDefaults.assistChipColors(
            containerColor = if (isDark) Color(0xFF733702) else Color(0xFFFFDCC0),
            labelColor = if (isDark) Color(0xFFFFB785) else Color(0xFF2F1500)
        )

        CaseResult.ADJOURNMENT -> AssistChipDefaults.assistChipColors(
            containerColor = if (isDark) Color(0xFF93000A) else Color(0xFFFFDAD6),
            labelColor = if (isDark) Color(0xFFFFDAD6) else Color(0xFF410002)
        )

        CaseResult.EXPERTISE -> AssistChipDefaults.assistChipColors(
            containerColor = if (isDark) Color(0xFF553571) else Color(0xFFF3DAFF),
            labelColor = if (isDark) Color(0xFFF3DAFF) else Color(0xFF250040)
        )

        CaseResult.RESTARTED -> AssistChipDefaults.assistChipColors(
            containerColor = if (isDark) Color(0xFF004D61) else Color(0xFFC7EFFF),
            labelColor = if (isDark) Color(0xFFC7EFFF) else Color(0xFF001F29)
        )

        CaseResult.DECISION -> AssistChipDefaults.assistChipColors(
            containerColor = if (isDark) Color(0xFF005316) else Color(0xFFC8E6C9),
            labelColor = if (isDark) Color(0xFFADF3A0) else Color(0xFF1B5E20)
        )
    }
}
