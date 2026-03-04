package com.chumakov123.casedocket.presentation.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Drafts
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.chumakov123.casedocket.R

@Composable
fun MainScreen(
    onNavigateToEditDraft: (Long) -> Unit,
    onNavigateToEditConfirmed: (Long) -> Unit // для этапа 6
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            BottomAppBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Drafts, contentDescription = null) },
                    label = { Text(stringResource(R.string.drafts)) }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.CheckCircle, contentDescription = null) },
                    label = { Text(stringResource(R.string.confirmed)) }
                )
            }
        }
    ) { paddingValues ->
        when (selectedTab) {
            0 -> DraftListScreen(
                onNavigateToEdit = onNavigateToEditDraft,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )

            1 -> ConfirmedListScreen(
                onEditClick = onNavigateToEditConfirmed,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        }
    }
}