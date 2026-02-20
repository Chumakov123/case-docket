package com.chumakov123.casedocket.presentation.screens.components

import android.app.TimePickerDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

@Composable
fun TimePickerDialog(
    initialTime: String, // в формате "HH:MM"
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }

    val initialParts = initialTime.split(":").mapNotNull { it.toIntOrNull() }
    if (initialParts.size == 2) {
        calendar.set(Calendar.HOUR_OF_DAY, initialParts[0])
        calendar.set(Calendar.MINUTE, initialParts[1])
    }

    val timePickerDialog = remember {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val formattedTime = LocalTime.of(hourOfDay, minute)
                    .format(DateTimeFormatter.ofPattern("HH:mm"))
                onConfirm(formattedTime)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true // true для 24-часового формата
        ).apply {
            setOnDismissListener { onDismiss() }
        }
    }

    LaunchedEffect(Unit) {
        timePickerDialog.show()
    }
}