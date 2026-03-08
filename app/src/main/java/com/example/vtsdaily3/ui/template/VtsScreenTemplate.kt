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
import com.example.vtsdaily3.ui.theme.VtsGreen

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
            .padding(top = 36.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // left spacer balances the menu width
            Box(modifier = Modifier.size(48.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )

            if (showControls) {
                Box(
                    modifier = Modifier.size(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    dropdown?.invoke()
                }
            } else {
                Box(modifier = Modifier.size(48.dp))
            }
        }

        VtsThickDivider()

        if (showControls) {
            searchBar?.invoke()
            sortBar?.invoke()
            VtsThinDivider()
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
            .padding(horizontal = 12.dp)
            .height(10.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(VtsGreen)
    )
}

@Composable
fun VtsThinDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.primary)
    )
}
