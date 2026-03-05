package com.chumakov123.casedocket.presentation.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.chumakov123.casedocket.R
import com.chumakov123.casedocket.presentation.viewmodel.SettingsViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val settings by viewModel.settings.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Язык
            Text(
                text = stringResource(R.string.language),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            LanguageOption(
                label = stringResource(R.string.russian),
                selected = settings.language == "ru",
                onClick = { viewModel.updateLanguage("ru") }
            )
            LanguageOption(
                label = stringResource(R.string.english),
                selected = settings.language == "en",
                onClick = { viewModel.updateLanguage("en") }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Тема
            Text(
                text = stringResource(R.string.theme),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            ThemeOption(
                label = stringResource(R.string.theme_system),
                selected = settings.theme == "system",
                onClick = { viewModel.updateTheme("system") }
            )
            ThemeOption(
                label = stringResource(R.string.theme_light),
                selected = settings.theme == "light",
                onClick = { viewModel.updateTheme("light") }
            )
            ThemeOption(
                label = stringResource(R.string.theme_dark),
                selected = settings.theme == "dark",
                onClick = { viewModel.updateTheme("dark") }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Уведомления
            Text(
                text = stringResource(R.string.notification_timing),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            val options = listOf(0, 5, 10, 15, 20, 25, 30)
            options.forEach { minutes ->
                NotificationOption(
                    minutes = minutes,
                    selected = settings.notificationMinutes == minutes,
                    onClick = { viewModel.updateNotificationMinutes(minutes) }
                )
            }
        }
    }
}

@Composable
fun LanguageOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        RadioButton(selected = selected, onClick = onClick)
        Text(text = label, modifier = Modifier.padding(start = 8.dp))
    }
}

@Composable
fun ThemeOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        RadioButton(selected = selected, onClick = onClick)
        Text(text = label, modifier = Modifier.padding(start = 8.dp))
    }
}

@Composable
fun NotificationOption(minutes: Int, selected: Boolean, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        RadioButton(selected = selected, onClick = onClick)
        Text(
            text = if (minutes == 0) stringResource(R.string.notification_off)
            else stringResource(R.string.notification_minutes, minutes),
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}