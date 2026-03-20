package com.example.vtsdaily3.ui.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

import com.example.vtsdaily3.ui.components.VtsOverflowMenu
import com.example.vtsdaily3.ui.components.VtsScreenHeader
import com.example.vtsdaily3.ui.components.VtsSearchField
import com.example.vtsdaily3.ui.components.VtsSortBar
import com.example.vtsdaily3.ui.theme.Vts3DailyTheme

@Composable
private fun VtsBarsPlaygroundScreen() {
    var searchValue by remember { mutableStateOf("Alice") }
    var selectedSortOption by remember { mutableStateOf("Name") }
    var menuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        VtsScreenHeader(
            title = "Passenger Lookup",
            subtitle = "Spacing playground",
            showDivider = false
        )

        VtsSearchField(
            value = searchValue,
            onValueChange = { searchValue = it },
            placeholder = "Search passengers"
        )

        VtsSortBar(
            options = listOf("Name", "Trips", "Recent"),
            selectedOption = selectedSortOption,
            onOptionSelected = { selectedSortOption = it }
        )

        VtsOverflowMenu(
            expanded = menuExpanded,
            onExpandedChange = { menuExpanded = it }
        ) {
            DropdownMenuItem(
                text = { Text("Test action 1") },
                onClick = { menuExpanded = false }
            )
            DropdownMenuItem(
                text = { Text("Test action 2") },
                onClick = { menuExpanded = false }
            )
        }

        Text(
            text = "Use this screen to tune spacing between header, search, sort, and menu.",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Preview(showBackground = true, widthDp = 420)
@Composable
private fun VtsBarsPlaygroundPreview() {
    Vts3DailyTheme {
        VtsBarsPlaygroundScreen()
    }
}

@Preview(showBackground = true, widthDp = 420)
@Composable
private fun VtsScreenHeaderPreviewOnly() {
    Vts3DailyTheme {
        VtsScreenHeader(
            title = "Passenger Lookup",
            subtitle = "Spacing playground",
            showDivider = false
        )
    }
}

@Preview(showBackground = true, widthDp = 420)
@Composable
private fun VtsSearchFieldPreviewOnly() {
    var searchValue by remember { mutableStateOf("Alice") }

    Vts3DailyTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            VtsSearchField(
                value = searchValue,
                onValueChange = { searchValue = it },
                placeholder = "Search passengers"
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 420)
@Composable
private fun VtsSortBarPreviewOnly() {
    var selectedSortOption by remember { mutableStateOf("Name") }

    Vts3DailyTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            VtsSortBar(
                options = listOf("Name", "Trips", "Recent"),
                selectedOption = selectedSortOption,
                onOptionSelected = { selectedSortOption = it }
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 420)
@Composable
private fun VtsOverflowMenuPreviewOnly() {
    var menuExpanded by remember { mutableStateOf(false) }

    Vts3DailyTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            VtsOverflowMenu(
                expanded = menuExpanded,
                onExpandedChange = { menuExpanded = it }
            ) {
                DropdownMenuItem(
                    text = { Text("Test action 1") },
                    onClick = { menuExpanded = false }
                )
                DropdownMenuItem(
                    text = { Text("Test action 2") },
                    onClick = { menuExpanded = false }
                )
            }
        }
    }
}