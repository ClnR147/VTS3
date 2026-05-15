package com.example.vtsdaily3.feature_schedule.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import com.example.vtsdaily3.ui.theme.VtsGreen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun AddScheduleBlockDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var blockText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add Time Block")
        },
        text = {
            OutlinedTextField(
                value = blockText,
                onValueChange = { blockText = it },
                singleLine = true,
                label = { Text("REFUEL 14:15-14:30") }
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(blockText)
                }
            ) {
                Text("Save", color = VtsGreen)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = VtsGreen)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp)
    )
}