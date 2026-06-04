package com.chumakov123.casedocket.presentation.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chumakov123.casedocket.R
import com.chumakov123.casedocket.presentation.theme.AppTheme
import com.chumakov123.casedocket.presentation.viewmodel.MainViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToEditDraft: (Long) -> Unit,
    onNavigateToEditConfirmed: (Long) -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: MainViewModel = koinViewModel()
) {
    val selectedTab by viewModel.selectedTab.collectAsState()

    MainContent(
        selectedTab = selectedTab,
        onTabSelected = { viewModel.selectTab(it) },
        onNavigateToSettings = onNavigateToSettings
    ) { tab ->
        when (tab) {
            0 -> DraftListScreen(
                onNavigateToEdit = onNavigateToEditDraft
            )

            1 -> ConfirmedListScreen(
                onEditClick = onNavigateToEditConfirmed
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainContent(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onNavigateToSettings: () -> Unit,
    content: @Composable (Int) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (selectedTab) {
                            0 -> stringResource(R.string.recognition)
                            else -> stringResource(R.string.schedule)
                        }
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = stringResource(R.string.settings)
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = NavigationBarItemDefaults().tonalElevation()
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { onTabSelected(0) },
                    icon = { Icon(Icons.Default.DocumentScanner, contentDescription = null) },
                    label = { Text(stringResource(R.string.recognition)) }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { onTabSelected(1) },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                    label = { Text(stringResource(R.string.schedule)) }
                )
            }
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier.padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            Crossfade(
                targetState = selectedTab,
                animationSpec = tween(durationMillis = 300),
                label = "tab_fade"
            ) { tab ->
                content(tab)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    AppTheme {
        MainContent(
            selectedTab = 0,
            onTabSelected = {},
            onNavigateToSettings = {}
        ) { tab ->
            Text("Tab $tab Content")
        }
    }
}

private class NavigationBarItemDefaults {
    fun tonalElevation() = 3.dp
}