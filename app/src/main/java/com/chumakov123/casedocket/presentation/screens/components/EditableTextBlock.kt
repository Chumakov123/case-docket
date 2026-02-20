package com.chumakov123.casedocket.presentation.screens.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

@Composable
fun EditableTextBlock(
    value: String?,
    placeholder: String,
    modifier: Modifier = Modifier.Companion,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    placeholderColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    minLines: Int = 1,
    maxLines: Int = Int.MAX_VALUE,
    onClick: () -> Unit
) {
    val isEmpty = value.isNullOrBlank()

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier.Companion
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Text(
                text = if (isEmpty) placeholder else value,
                style = textStyle,
                color = if (isEmpty) placeholderColor else textColor,
                minLines = minLines,
                maxLines = maxLines
            )
        }
    }
}