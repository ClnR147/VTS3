package com.example.vtsdaily3.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.vtsdaily3.ui.theme.VtsSpacing

@Composable
fun VtsListScreenHeader(
    title: String,
    subtitle: String? = null,
    showDivider: Boolean = true,
    searchBar: (@Composable () -> Unit)? = null,
    sortBar: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(VtsSpacing.md)
    ) {
        VtsScreenHeader(
            title = title,
            subtitle = subtitle,
            showDivider = showDivider
        )

        if (searchBar != null) {
            searchBar()
        }

        if (sortBar != null) {
            sortBar()
        }
    }
}