package com.chumakov123.casedocket.presentation.screens.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.chumakov123.casedocket.R

@Composable
fun ErrorState(message: String, onRetry: () -> Unit) {
    Card(
        modifier = Modifier.Companion.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.Companion
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.Companion.CenterHorizontally
        ) {
            Icon(
                Icons.Default.ErrorOutline,
                contentDescription = null,
                modifier = Modifier.Companion.size(56.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.Companion.height(16.dp))
            Text(
                stringResource(R.string.error_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.Companion.height(12.dp))
            Text(
                message.ifEmpty { stringResource(R.string.error_default_message) },
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Companion.Center
            )
            Spacer(modifier = Modifier.Companion.height(24.dp))
            Button(
                onClick = onRetry,
                modifier = Modifier.Companion.fillMaxWidth()
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.Companion.width(8.dp))
                Text(stringResource(R.string.retry_button))
            }
        }
    }
}