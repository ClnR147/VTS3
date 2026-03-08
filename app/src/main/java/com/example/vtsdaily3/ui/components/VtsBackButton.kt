package com.example.vtsdaily3.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.vtsdaily3.ui.theme.VtsGreen
import com.example.vtsdaily3.ui.theme.VtsShapes
import com.example.vtsdaily3.ui.theme.VtsSpacing

@Composable
fun VtsBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = VtsShapes.fab,
        color = VtsGreen,
        tonalElevation = VtsSpacing.fabElevation,
        shadowElevation = VtsSpacing.fabElevation
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(VtsSpacing.backButtonSize)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }
    }
}