package com.chumakov123.casedocket.presentation.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Drafts
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.chumakov123.casedocket.R
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (selectedTab) {
                            0 -> stringResource(R.string.drafts)
                            else -> stringResource(R.string.confirmed)
                        }
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { viewModel.selectTab(0) },
                    icon = { Icon(Icons.Default.Drafts, contentDescription = null) },
                    label = { Text(stringResource(R.string.drafts)) }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { viewModel.selectTab(1) },
                    icon = { Icon(Icons.Default.CheckCircle, contentDescription = null) },
                    label = { Text(stringResource(R.string.confirmed)) }
                )
            }
        }
    ) { paddingValues ->
        when (selectedTab) {
            0 -> DraftListScreen(
                onNavigateToEdit = onNavigateToEditDraft,
                modifier = Modifier.padding(paddingValues)
            )

            1 -> ConfirmedListScreen(
                onEditClick = onNavigateToEditConfirmed,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}