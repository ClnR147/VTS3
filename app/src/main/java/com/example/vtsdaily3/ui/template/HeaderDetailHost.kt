package com.example.vtsdaily3.ui.template

import androidx.compose.runtime.Composable

@Composable
fun <T> HeaderDetailHost(
    selectedItem: T?,
    onSelectItem: (T) -> Unit,
    onClearSelection: () -> Unit,
    summary: @Composable (onSelect: (T) -> Unit) -> Unit,
    detail: @Composable (item: T, onBack: () -> Unit) -> Unit
) {

    if (selectedItem == null) {

        summary(onSelectItem)

    } else {

        detail(
            selectedItem,
            onClearSelection
        )
    }
}