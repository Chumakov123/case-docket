package com.chumakov123.casedocket.presentation.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.DocumentScanner
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.chumakov123.casedocket.R

@Composable
fun IdleState(onTestClick: () -> Unit) {
    Card(
        modifier = Modifier.Companion.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.Companion
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.Companion.CenterHorizontally
        ) {
            Icon(
                Icons.Default.DocumentScanner,
                contentDescription = null,
                modifier = Modifier.Companion.size(72.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.Companion.height(24.dp))
            Text(
                stringResource(R.string.test_recognition),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Companion.Bold,
                textAlign = TextAlign.Companion.Center
            )
            Spacer(modifier = Modifier.Companion.height(16.dp))
            Text(
                stringResource(R.string.test_recognition_description),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Companion.Center
            )
            Spacer(modifier = Modifier.Companion.height(32.dp))
            Button(
                onClick = onTestClick,
                modifier = Modifier.Companion.fillMaxWidth()
            ) {
                Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = null)
                Spacer(modifier = Modifier.Companion.width(8.dp))
                Text(stringResource(R.string.start_test_recognition))
            }
        }
    }
}