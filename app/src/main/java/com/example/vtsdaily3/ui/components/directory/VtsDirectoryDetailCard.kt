package com.example.vtsdaily3.ui.components.directory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.vtsdaily3.ui.components.VtsCard
import com.example.vtsdaily3.ui.theme.VtsSpacing

@Composable
fun VtsDirectoryDetailCard(
    title: String,
    actions: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    VtsCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(VtsSpacing.md)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall
                )

                if (actions != null) {
                    actions()
                }
            }

            VtsThinDivider()

            content()
        }
    }
}