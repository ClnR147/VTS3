package com.example.vtsdaily3.ui.template

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.vtsdaily3.ui.theme.VtsDivider
import com.example.vtsdaily3.ui.theme.VtsGreen
import com.example.vtsdaily3.ui.theme.VtsSpacing

@Composable
fun VtsScreenTemplate(
    title: String,
    showControls: Boolean = true,
    dropdown: (@Composable () -> Unit)? = null,
    searchBar: (@Composable () -> Unit)? = null,
    sortBar: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = VtsSpacing.sm)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = VtsSpacing.md, vertical = VtsSpacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(VtsSpacing.headerButtonSize)
            )

            Text(
                text = title,
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 12.dp, bottom = 8.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Box(
                modifier = Modifier
                    .size(VtsSpacing.headerButtonSize)
                    .offset(y = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                if (showControls) {
                    dropdown?.invoke()
                }
            }

        }

        Box(
            modifier = Modifier.padding(horizontal = VtsSpacing.md)
        ) {

        }


        if (showControls) {

            Spacer(Modifier.height(VtsSpacing.xs))

            Column(
                verticalArrangement = Arrangement.spacedBy(VtsSpacing.xs)
            ) {
                searchBar?.invoke()
                sortBar?.invoke()
                VtsThinDivider()
            }

            Spacer(Modifier.height(VtsSpacing.xs))
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            content()
        }
    }
}

@Composable
fun VtsThickDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(VtsDivider.thickHeight)
            .clip(RoundedCornerShape(VtsDivider.thickCornerRadius))
            .background(VtsGreen)
    )
}

@Composable
fun VtsThinDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = VtsSpacing.md)
            .height(VtsDivider.thinHeight)
            .background(VtsGreen)
    )
}