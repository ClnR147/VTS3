package com.example.vtsdaily3.ui.components.directory

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun VtsInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}