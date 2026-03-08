package com.example.vtsdaily3.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.example.vtsdaily3.ui.theme.VtsGreen
import com.example.vtsdaily3.ui.theme.VtsSpacing

@Composable
fun VtsScreenHeader(
    title: String,
    subtitle: String? = null,
    showDivider: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(VtsSpacing.xs)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        if (!subtitle.isNullOrBlank()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.titleMedium,
                color = VtsGreen
            )
        }

        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(top = VtsSpacing.sm),
                color = VtsGreen
            )
        }
    }
}