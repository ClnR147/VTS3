package com.example.vtsdaily3.ui.components.directory

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.vtsdaily3.ui.theme.VtsGreen

@Composable
fun VtsThinDivider() {
    HorizontalDivider(
        color = VtsGreen.copy(alpha = 0.25f), // 👈 soft, not loud
        thickness = 1.dp
    )
}