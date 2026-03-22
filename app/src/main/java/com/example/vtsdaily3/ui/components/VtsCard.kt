package com.example.vtsdaily3.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.vtsdaily3.ui.theme.LightGreenCardBackground
import com.example.vtsdaily3.ui.theme.VtsShapes
import com.example.vtsdaily3.ui.theme.VtsSpacing

enum class VtsCardDensity {
    Default,
    Compact
}

@Composable
fun VtsCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    density: VtsCardDensity = VtsCardDensity.Default,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardModifier = when {
        onClick != null || onLongClick != null -> {
            modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = { onClick?.invoke() },
                    onLongClick = { onLongClick?.invoke() }
                )
        }
        else -> {
            modifier.fillMaxWidth()
        }
    }

    val verticalPadding = when (density) {
        VtsCardDensity.Default -> VtsSpacing.md
        VtsCardDensity.Compact -> VtsSpacing.sm
    }

    Card(
        modifier = cardModifier,
        shape = VtsShapes.card,
        colors = CardDefaults.cardColors(
            containerColor = LightGreenCardBackground
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = VtsSpacing.cardElevation
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = VtsSpacing.md,
                    vertical = verticalPadding
                ),
            content = content
        )
    }
}