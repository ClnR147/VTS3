package com.example.vtsdaily3.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.vtsdaily3.lookup.LookupRow
import com.example.vtsdaily3.ui.template.HeaderDetailHost
import com.example.vtsdaily3.ui.template.VtsScreenTemplate
import java.util.Locale

@Composable
fun LookupScreen() {
    var searchQuery by remember { mutableStateOf("") }
    var selectedPassengerKey by remember { mutableStateOf<String?>(null) }
    var menuExpanded by remember { mutableStateOf(false) }
    var sortMode by remember { mutableStateOf(LookupSortMode.NAME) }
    var importedRows by remember { mutableStateOf<List<LookupRow>>(emptyList()) }

    val summaries = remember(importedRows) {
        buildPassengerSummaries(importedRows)
    }

    val filteredSummaries = remember(summaries, searchQuery, sortMode) {
        summaries
            .filter { summary ->
                val q = searchQuery.trim().lowercase(Locale.getDefault())
                q.isBlank() ||
                        summary.passenger.lowercase(Locale.getDefault()).contains(q) ||
                        summary.phone.orEmpty().lowercase(Locale.getDefault()).contains(q)
            }
            .let { list ->
                when (sortMode) {
                    LookupSortMode.NAME ->
                        list.sortedWith(
                            compareBy<LookupPassengerSummary> { it.passenger.lowercase(Locale.getDefault()) }
                                .thenByDescending { it.tripCount }
                        )

                    LookupSortMode.TRIPS ->
                        list.sortedWith(
                            compareByDescending<LookupPassengerSummary> { it.tripCount }
                                .thenBy { it.passenger.lowercase(Locale.getDefault()) }
                        )
                }
            }
    }

    val selectedSummary = remember(filteredSummaries, selectedPassengerKey, summaries) {
        val source = if (selectedPassengerKey == null) emptyList() else summaries
        source.firstOrNull { it.key == selectedPassengerKey }
    }

    VtsScreenTemplate(
        title = if (selectedSummary == null) "Passenger Lookup" else selectedSummary.passenger,
        showControls = selectedSummary == null,

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
                        onClick = {
                            menuExpanded = false
                            // TODO: wire CSV import here
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Save") },
                        onClick = {
                            menuExpanded = false
                            // TODO: wire save here if needed
                        }
                    )
                }
            }
        },

        searchBar = {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search passengers") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                singleLine = true
            )
        },

        sortBar = {
            LookupSortBar(
                selected = sortMode,
                onSortName = { sortMode = LookupSortMode.NAME },
                onSortTrips = { sortMode = LookupSortMode.TRIPS }
            )
        }
    ) {
        HeaderDetailHost(
            selectedItem = selectedPassengerKey,
            onSelectItem = { selectedPassengerKey = it },
            onClearSelection = { selectedPassengerKey = null },

            summary = { onSelect ->
                when {
                    importedRows.isEmpty() -> {
                        LookupEmptyState(
                            message = "No lookup data loaded yet.\nUse the menu to import a CSV."
                        )
                    }

                    filteredSummaries.isEmpty() -> {
                        LookupEmptyState(
                            message = "No passengers matched your search."
                        )
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(
                                items = filteredSummaries,
                                key = { it.key }
                            ) { summary ->
                                LookupSummaryCard(
                                    summary = summary,
                                    onClick = { onSelect(summary.key) }
                                )
                            }
                        }
                    }
                }
            },

            detail = { _, onBack ->
                val summary = selectedSummary

                if (summary == null) {
                    LookupEmptyState(message = "Passenger not found.")
                } else {
                    LookupDetailScreen(
                        summary = summary,
                        onBack = onBack
                    )
                }
            }
        )
    }
}

@Composable
private fun LookupSummaryCard(
    summary: LookupPassengerSummary,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = summary.passenger,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = summary.tripCount.toString(),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(72.dp)
            )
        }
    }
}

@Composable
private fun LookupDetailScreen(
    summary: LookupPassengerSummary,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Text(
            text = summary.passenger,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        if (!summary.phone.isNullOrBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = summary.phone,
                style = MaterialTheme.typography.titleMedium
            )
        }

        Spacer(Modifier.height(12.dp))

        Button(onClick = onBack) {
            Text("Back")
        }

        Spacer(Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = summary.trips,
                key = { tripRowKey(it) }
            ) { trip ->
                LookupTripCard(trip = trip)
            }
        }
    }
}

@Composable
private fun LookupTripCard(trip: LookupRow) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            LookupLabelValueRow(
                label = "Date:",
                value = trip.driveDate.orEmpty()
            )

            LookupLabelValueRow(
                label = "Pickup:",
                value = trip.pAddress.orEmpty()
            )

            LookupLabelValueRow(
                label = "Drop-off:",
                value = trip.dAddress.orEmpty()
            )
        }
    }
}

@Composable
private fun LookupLabelValueRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(80.dp)
        )

        Text(
            text = value.ifBlank { "-" },
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun LookupEmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun LookupSortBar(
    selected: LookupSortMode,
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
                Text(if (selected == LookupSortMode.NAME) "Name ✓" else "Name")
            }
        }

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Button(onClick = onSortTrips) {
                Text(if (selected == LookupSortMode.TRIPS) "Trips ✓" else "Trips")
            }
        }
    }
}

private enum class LookupSortMode {
    NAME,
    TRIPS
}

private data class LookupPassengerSummary(
    val key: String,
    val passenger: String,
    val phone: String?,
    val tripCount: Int,
    val trips: List<LookupRow>
)

private fun buildPassengerSummaries(rows: List<LookupRow>): List<LookupPassengerSummary> {
    return rows
        .filter { !it.passenger.isNullOrBlank() }
        .groupBy { normalizePassengerKey(it.passenger.orEmpty()) }
        .mapNotNull { (key, trips) ->
            if (key.isBlank()) return@mapNotNull null

            val displayName = trips
                .mapNotNull { it.passenger?.trim() }
                .firstOrNull { it.isNotBlank() }
                ?: return@mapNotNull null

            val phone = trips
                .mapNotNull { it.phone?.trim() }
                .firstOrNull { it.isNotBlank() }

            LookupPassengerSummary(
                key = key,
                passenger = displayName,
                phone = phone,
                tripCount = trips.size,
                trips = trips.sortedWith(
                    compareBy<LookupRow>(
                        { sortableDateValue(it.driveDate) },
                        { it.pAddress.orEmpty().lowercase(Locale.getDefault()) },
                        { it.dAddress.orEmpty().lowercase(Locale.getDefault()) }
                    )
                )
            )
        }
}

private fun normalizePassengerKey(name: String): String {
    return name.trim().lowercase(Locale.getDefault())
}

private fun sortableDateValue(raw: String?): String {
    val value = raw.orEmpty().trim()
    if (value.isBlank()) return "9999-99-99"

    val parts = value.split("/", "-", ".")
    return if (parts.size == 3) {
        val a = parts[0].padStart(2, '0')
        val b = parts[1].padStart(2, '0')
        val c = parts[2]

        when {
            c.length == 4 -> "$c-$a-$b"
            c.length == 2 -> "20$c-$a-$b"
            else -> value
        }
    } else {
        value
    }
}

private fun tripRowKey(row: LookupRow): String {
    return listOf(
        row.driveDate.orEmpty(),
        row.passenger.orEmpty(),
        row.pAddress.orEmpty(),
        row.dAddress.orEmpty(),
        row.tripType.orEmpty(),
        row.puTimeAppt.orEmpty(),
        row.doTimeAppt.orEmpty(),
        row.rtTime.orEmpty()
    ).joinToString("|")
}
