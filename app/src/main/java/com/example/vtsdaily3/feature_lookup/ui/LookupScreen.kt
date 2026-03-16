package com.example.vtsdaily3.feature_lookup.ui

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.example.vtsdaily3.feature_lookup.data.LookupRow
import com.example.vtsdaily3.feature_lookup.data.importLookupCsv
import com.example.vtsdaily3.ui.template.HeaderDetailHost
import com.example.vtsdaily3.ui.template.VtsScreenTemplate
import com.example.vtsdaily3.ui.theme.VtsGreen
import java.time.LocalDate
import java.util.Locale
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color
import androidx.core.content.edit
import androidx.core.net.toUri
import com.example.vtsdaily3.feature_lookup.data.LookupStore
import com.example.vtsdaily3.feature_lookup.domain.LookupPassengerDetail
import com.example.vtsdaily3.feature_lookup.domain.LookupSummary
import com.example.vtsdaily3.feature_lookup.domain.LookupTripDetail
import com.example.vtsdaily3.feature_lookup.domain.buildLookupPassengerDetail
import com.example.vtsdaily3.feature_lookup.ui.state.buildLookupUiState
import com.example.vtsdaily3.ui.components.VtsBackButton
import com.example.vtsdaily3.ui.components.VtsCard
import com.example.vtsdaily3.ui.components.VtsCardDensity
import com.example.vtsdaily3.ui.components.VtsScreenHeader
import com.example.vtsdaily3.ui.components.VtsSearchField
import com.example.vtsdaily3.ui.components.VtsSummaryRow
import com.example.vtsdaily3.ui.theme.VtsSpacing
import com.example.vtsdaily3.util.VtsDateFormat
import com.example.vtsdaily3.feature_lookup.ui.state.LookupUiState
import com.example.vtsdaily3.ui.template.VtsThinDivider

@Composable
fun LookupScreen(
    initialPassengerName: String? = null,
    onInitialPassengerNameConsumed: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedPassengerName by remember { mutableStateOf<String?>(null) }
    var menuExpanded by remember { mutableStateOf(false) }
    var sortMode by remember { mutableStateOf(LookupSortMode.NAME) }
    var uiState by remember { mutableStateOf(LookupUiState()) }
    var lastOpenedPassengerName by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    LaunchedEffect(initialPassengerName) {
        initialPassengerName
            ?.takeIf { it.isNotBlank() }
            ?.let { passengerName ->
                searchQuery = passengerName
                selectedPassengerName = passengerName
                onInitialPassengerNameConsumed()
            }
    }

    val selectedDetail = remember(uiState.rows, selectedPassengerName) {
        selectedPassengerName?.let { passengerName ->
            buildLookupPassengerDetail(uiState.rows, passengerName)
        }
    }

    LaunchedEffect(Unit) {
        uiState = buildLookupUiState(LookupStore.load(context))
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
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: SecurityException) {
            // Fine if persistable permission is not available
        }

        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                val rows = importLookupCsv(input)
                uiState.rows = rows
                LookupStore.save(context, rows)
                selectedPassengerName = null
                searchQuery = ""
                saveLookupUri(context, uri)
            }
        } catch (e: Exception) {
            Log.e("LookupImport", "Import failed", e)
        }
    }


    val summaryListState = rememberSaveable(saver = LazyListState.Saver) {
        LazyListState(0, 0)
    }

    val queryText = remember(searchQuery) {
        searchQuery.trim()
    }

    val filteredSummaries = remember(uiState.summaries, queryText, sortMode) {
        uiState.summaries
            .filter { summary ->
                queryText.isBlank() ||
                        summary.passenger.contains(queryText, ignoreCase = true)
            }
            .let { list ->
                when (sortMode) {
                    LookupSortMode.NAME ->
                        list.sortedWith(
                            compareBy<LookupSummary> { it.passenger.lowercase(Locale.getDefault()) }
                                .thenByDescending { it.tripCount }
                        )

                    LookupSortMode.TRIPS ->
                        list.sortedWith(
                            compareByDescending<LookupSummary> { it.tripCount }
                                .thenBy { it.passenger.lowercase(Locale.getDefault()) }
                        )
                }
            }
    }

    val selectedSummary = remember(selectedPassengerName, uiState.summaries) {
        selectedPassengerName?.let { name ->
            uiState.summaries.firstOrNull { it.passenger == name }
        }
    }

    LaunchedEffect(queryText) {
        if (selectedPassengerName == null) {
            summaryListState.scrollToItem(0)
        }
    }

    LaunchedEffect(sortMode) {
        if (selectedPassengerName == null) {
            summaryListState.scrollToItem(0)
        }
    }

    LaunchedEffect(selectedPassengerName) {
        if (selectedPassengerName == null) {
            summaryListState.scrollToItem(0)
        }
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
            Box(
                modifier = Modifier.padding(
                    start = VtsSpacing.sm,
                    end = VtsSpacing.sm,
                    top = 0.dp
                )
            ) {
                VtsSearchField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        selectedPassengerName = null
                    },
                    placeholder = "Search passengers"
                )
            }
        }
        ,

        sortBar = {
            LookupSortBar(
                selected = sortMode,
                onSortName = { sortMode = LookupSortMode.NAME },
                onSortTrips = { sortMode = LookupSortMode.TRIPS }
            )
        }
    ) {
        HeaderDetailHost(
            selectedItem = selectedPassengerName,
            onSelectItem = {
                selectedPassengerName = it
            },
            onClearSelection = { selectedPassengerName = null },

            summary = { onSelect ->
                when {
                    uiState.rows.isEmpty() -> {
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
                            state = summaryListState
                        ) {
                            items(
                                items = filteredSummaries,
                                key = { it.passenger }
                            ) { summary ->
                                LookupSummaryCard(
                                    summary = summary,
                                    onClick = { onSelect(summary.passenger) }
                                )
                            }
                        }
                    }
                }
            },

            detail = { _, onBack ->
                if (selectedDetail == null) {
                    LookupEmptyState(message = "Passenger not found.")
                } else {
                    VtsThinDivider()
                    LookupDetailScreen(
                        detail = selectedDetail,
                        onBack = onBack
                    )
                }
            }
        )
    }
}

@Composable
private fun LookupSummaryCard(
    summary: LookupSummary,
    onClick: () -> Unit
) {
    VtsCard(
        onClick = onClick,
        density = VtsCardDensity.Compact
    ) {
        VtsSummaryRow(
            title = summary.passenger,
            trailingText = summary.tripCount.toString()
        )
    }
}
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LookupDetailScreen(
    detail: LookupPassengerDetail,
    onBack: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(VtsSpacing.md)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = VtsSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(VtsSpacing.md)
        ) {
            stickyHeader {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(bottom = VtsSpacing.xs)
                ) {
                    VtsScreenHeader(
                        title = detail.passenger,
                        subtitle = detail.phone,
                        showDivider = false
                    )
                }
            }

            items(detail.dayGroups) { dayGroup ->
                LookupTripDateCard(
                    date = dayGroup.driveDate.orEmpty(),
                    trips = dayGroup.trips
                )
            }

            item {
                Spacer(modifier = Modifier.height(VtsSpacing.fabClearance))
            }
        }

        VtsBackButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .navigationBarsPadding()
                .padding(start = VtsSpacing.lg, bottom = VtsSpacing.lg)
        )
    }
}


@Composable
private fun LookupTripDateCard(
    date: String,
    trips: List<LookupTripDetail>
) {
    VtsCard {
        Text(
            text = date,
            style = MaterialTheme.typography.titleMedium,
            color = VtsGreen,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(6.dp))

        Spacer(Modifier.height(VtsSpacing.sm))

        trips.forEachIndexed { index, trip ->
            if (index > 0) {
                Spacer(Modifier.height(VtsSpacing.sm))
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(VtsSpacing.xs)
            ) {
                LookupLabelValueRow(
                    label = "Pickup:",
                    value = trip.pickup
                )
                LookupLabelValueRow(
                    label = "Drop-off:",
                    value = trip.dropoff
                )
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
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .width(72.dp)
                .padding(end = 4.dp)
        )

        Text(
            text = value,
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
            .padding(horizontal = VtsSpacing.md),
        horizontalArrangement = Arrangement.spacedBy(VtsSpacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val selectedColors = ButtonDefaults.buttonColors(
            containerColor = VtsGreen,
            contentColor = Color.White
        )

        val unselectedColors = ButtonDefaults.outlinedButtonColors(
            contentColor = VtsGreen
        )

        if (selected == LookupSortMode.NAME) {
            Button(
                onClick = onSortName,
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp),
                colors = selectedColors,
                shape = RoundedCornerShape(50),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text("Name")
            }
        } else {
            OutlinedButton(
                onClick = onSortName,
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp),
                colors = unselectedColors,
                shape = RoundedCornerShape(50),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text("Name")
            }
        }

        if (selected == LookupSortMode.TRIPS) {
            Button(
                onClick = onSortTrips,
                modifier = Modifier.weight(1f),
                colors = selectedColors
            ) {
                Text("Trips")
            }
        } else {
            OutlinedButton(
                onClick = onSortTrips,
                modifier = Modifier.weight(1f),
                colors = unselectedColors
            ) {
                Text("Trips")
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
                    compareBy(
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
        VtsDateFormat.mmddyyyy(parsed)
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


