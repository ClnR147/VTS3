package com.example.vtsdaily3.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.vtsdaily3.ui.template.HeaderDetailHost
import com.example.vtsdaily3.ui.template.VtsScreenTemplate

@Composable
fun TemplateTestScreen() {

    var searchQuery by remember { mutableStateOf("") }
    var selectedRow by remember { mutableStateOf<Int?>(null) }
    var menuExpanded by remember { mutableStateOf(false) }

    VtsScreenTemplate(
        title = "Template Test",
        showControls = selectedRow == null,

        dropdown = {
            Box {

                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Menu"
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Import") },
                        onClick = { menuExpanded = false }
                    )

                    DropdownMenuItem(
                        text = { Text("Save") },
                        onClick = { menuExpanded = false }
                    )
                }
            }
        },

        searchBar = {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                singleLine = true
            )
        },

        sortBar = {
            TemplateColumnSortBar(
                onSortName = { },
                onSortTrips = { }
            )
        }
    ) {

        HeaderDetailHost(
            selectedItem = selectedRow,
            onSelectItem = { selectedRow = it },
            onClearSelection = { selectedRow = null },

            summary = { onSelect ->

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                ) {

                    repeat(10) { index ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { onSelect(index) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Row ${index + 1}",
                                    modifier = Modifier.weight(1f)
                                )

                                Text(
                                    text = "${(index + 1) * 2}",
                                    modifier = Modifier.width(72.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            },

            detail = { item, onBack ->

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                ) {

                    Text(
                        text = "Detail Screen",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(Modifier.height(16.dp))

                    Text("You selected Row ${item + 1}")

                    Spacer(Modifier.height(16.dp))

                    Button(onClick = onBack) {
                        Text("Back")
                    }
                }
            }
        )
    }
}

@Composable
fun TemplateColumnSortBar(
    onSortName: () -> Unit,
    onSortTrips: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Button(onClick = onSortName) {
                Text("Name")
            }
        }

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Button(onClick = onSortTrips) {
                Text("Trips")
            }
        }
    }
}