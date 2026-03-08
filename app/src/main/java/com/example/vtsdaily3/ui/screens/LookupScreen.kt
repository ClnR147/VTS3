package com.example.vtsdaily3.ui.screens

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.vtsdaily3.lookup.LookupRow
import com.example.vtsdaily3.lookup.importLookupCsv
import com.example.vtsdaily3.ui.template.HeaderDetailHost
import com.example.vtsdaily3.ui.template.VtsScreenTemplate
import com.example.vtsdaily3.ui.theme.VtsGreen
import java.time.LocalDate
import java.util.Locale
import android.content.Context
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.core.content.edit
import androidx.core.net.toUri

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
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val savedUri = loadLookupUri(context) ?: return@LaunchedEffect

        runCatching {
            context.contentResolver.openInputStream(savedUri)?.use { input ->
                importedRows = importLookupCsv(input)
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) {
            Log.d("LookupImport", "Import cancelled")
            return@rememberLauncherForActivityResult
        }

        try {
            context.contentResolver.takePersistableUriPermission(
                uri,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: SecurityException) {
            // Fine if persistable permission is not available
        }

        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                val rows = importLookupCsv(input)
                importedRows = rows
                selectedPassengerKey = null
                searchQuery = ""
                saveLookupUri(context, uri)
            }
        } catch (e: Exception) {
            Log.e("LookupImport", "Import failed", e)
        }
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
        title = "Passenger Lookup",
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
                            importLauncher.launch(arrayOf("text/*", "*/*"))
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
    val tripsByDate = remember(summary.trips) {
        summary.trips
            .groupBy { normalizedDisplayDate(it.driveDate) }
            .toList()
            .sortedBy { (_, tripsForDate) ->
                tripsForDate.minOfOrNull {
                    parseLookupDate(it.driveDate) ?: LocalDate.MAX
                } ?: LocalDate.MAX
            }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = summary.passenger,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )

                    if (!summary.phone.isNullOrBlank()) {
                        Text(
                            text = summary.phone,
                            style = MaterialTheme.typography.titleMedium,
                            color = VtsGreen,
                            textAlign = TextAlign.End
                        )
                    }
                }
            }

            items(tripsByDate) { (date, tripsForDate) ->
                LookupTripDateCard(
                    date = date,
                    trips = tripsForDate
                )
            }

            item {
                Spacer(modifier = Modifier.height(84.dp))
            }
        }

        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .navigationBarsPadding()
                .padding(start = 20.dp, bottom = 20.dp)
                .size(64.dp)
                .clip(CircleShape)
                .background(VtsGreen)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }
    }
}
@Composable
private fun LookupTripDateCard(
    date: String,
    trips: List<LookupRow>
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Text(
                text = date,
                style = MaterialTheme.typography.titleMedium,
                color = VtsGreen,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(4.dp))

            trips.forEachIndexed { index, trip ->
                if (index > 0) {
                    Spacer(Modifier.height(4.dp))
                }

                Column {
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
                        { parseLookupDate(it.driveDate) ?: LocalDate.MAX },
                        { it.pAddress.orEmpty().trim().lowercase(Locale.getDefault()) },
                        { it.dAddress.orEmpty().trim().lowercase(Locale.getDefault()) },
                        { it.tripType.orEmpty().trim().lowercase(Locale.getDefault()) }
                    )
                )
            )
        }
}

private fun normalizedDisplayDate(raw: String?): String {
    val parsed = parseLookupDate(raw)
    return if (parsed != null) {
        parsed.toString()   // yyyy-MM-dd
    } else {
        raw.orEmpty().trim().ifBlank { "Unknown date" }
    }
}


private fun parseLookupDate(raw: String?): LocalDate? {
    val s = raw?.trim().orEmpty()
    if (s.isBlank()) return null

    val r1 = Regex("""(20\d{2})[./-](\d{1,2})[./-](\d{1,2})""")   // yyyy-mm-dd
    val r2 = Regex("""(\d{1,2})[./-](\d{1,2})[./-](\d{2})""")     // mm-dd-yy
    val r3 = Regex("""(\d{1,2})[./-](\d{1,2})[./-](20\d{2})""")   // mm-dd-yyyy

    r1.matchEntire(s)?.let {
        return safeDate(
            it.groupValues[1].toInt(),
            it.groupValues[2].toInt(),
            it.groupValues[3].toInt()
        )
    }

    r3.matchEntire(s)?.let {
        return safeDate(
            it.groupValues[3].toInt(),
            it.groupValues[1].toInt(),
            it.groupValues[2].toInt()
        )
    }

    r2.matchEntire(s)?.let {
        val yy = it.groupValues[3].toInt()
        return safeDate(
            2000 + yy,
            it.groupValues[1].toInt(),
            it.groupValues[2].toInt()
        )
    }

    return null
}

private fun safeDate(year: Int, month: Int, day: Int): LocalDate? {
    return try {
        LocalDate.of(year, month, day)
    } catch (_: Exception) {
        null
    }
}
private fun normalizePassengerKey(name: String): String {
    return name.trim().lowercase(Locale.getDefault())
}

private const val LOOKUP_PREFS = "lookup_prefs"
private const val KEY_LOOKUP_URI = "lookup_uri"

private fun saveLookupUri(context: Context, uri: Uri) {
    context.getSharedPreferences(LOOKUP_PREFS, Context.MODE_PRIVATE)
        .edit {
            putString(KEY_LOOKUP_URI, uri.toString())
        }
}

private fun loadLookupUri(context: Context): Uri? {
    val value = context.getSharedPreferences(LOOKUP_PREFS, Context.MODE_PRIVATE)
        .getString(KEY_LOOKUP_URI, null)
        ?: return null

    return runCatching { value.toUri() }.getOrNull()
}


