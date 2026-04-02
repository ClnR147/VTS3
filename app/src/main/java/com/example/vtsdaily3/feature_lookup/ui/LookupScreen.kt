package com.example.vtsdaily3.feature_lookup.ui

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.vtsdaily3.feature_lookup.data.importLookupCsv
import com.example.vtsdaily3.ui.theme.VtsGreen
import java.util.Locale
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.core.content.edit
import com.example.vtsdaily3.feature_lookup.audit.LookupAddressAudit
import com.example.vtsdaily3.feature_lookup.audit.LookupAuditCsvExporter
import com.example.vtsdaily3.feature_lookup.data.LookupRow
import com.example.vtsdaily3.feature_lookup.data.LookupStore
import com.example.vtsdaily3.feature_lookup.domain.LookupPassengerDetail
import com.example.vtsdaily3.feature_lookup.domain.LookupSummary
import com.example.vtsdaily3.feature_lookup.domain.LookupTripDetail
import com.example.vtsdaily3.feature_lookup.domain.buildLookupPassengerDetail
import com.example.vtsdaily3.feature_lookup.ui.state.buildLookupUiState
import com.example.vtsdaily3.ui.components.VtsCard
import com.example.vtsdaily3.ui.components.VtsCardDensity
import com.example.vtsdaily3.ui.components.VtsSummaryRow
import com.example.vtsdaily3.ui.theme.VtsSpacing
import com.example.vtsdaily3.feature_lookup.ui.state.LookupUiState
import com.example.vtsdaily3.feature_lookup.util.normalizePassengerNameForLookup
import com.example.vtsdaily3.model.Trip
import com.example.vtsdaily3.model.TripId
import com.example.vtsdaily3.ui.components.VtsOverflowMenu
import com.example.vtsdaily3.ui.components.directory.VtsDirectoryDetailCard
import com.example.vtsdaily3.ui.components.directory.VtsDirectoryScreenShell
import com.example.vtsdaily3.ui.components.directory.VtsInfoRow
import java.time.LocalDate


@Composable
fun LookupScreen(
    initialPassengerName: String? = null,
    onInitialPassengerNameConsumed: () -> Unit = {}
) {
    val context = LocalContext.current
    var uiState by remember { mutableStateOf(LookupUiState()) }

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
                uiState = buildLookupUiState(rows)
                LookupStore.save(context, rows)
                saveLookupUri(context, uri)
            }
        } catch (e: Exception) {
            Log.e("LookupImport", "Import failed", e)
        }
    }

    val auditExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult

        try {
            val rows = LookupStore.load(context)
            val report = LookupAddressAudit.run(rows)
            val csv = LookupAuditCsvExporter.toCsv(report)

            context.contentResolver.openOutputStream(uri)?.use { output ->
                output.write(csv.toByteArray())
            }

        } catch (e: Exception) {
            Log.e("LOOKUP_AUDIT", "Failed to export CSV", e)
        }
    }

    LookupScreenContent(
        uiState = uiState,
        onImportClick = {
            importLauncher.launch(arrayOf("text/*", "*/*"))
        },
        onRunDuplicateAddressAudit = {
            val report = LookupAddressAudit.run(LookupStore.load(context))

        },
        onExportDuplicateAddressAuditCsv = {
            auditExportLauncher.launch("lookup_duplicate_address_audit.csv")
        },
        initialPassengerName = initialPassengerName,
        onInitialPassengerNameConsumed = onInitialPassengerNameConsumed
    )
}

@Composable
private fun LookupScreenContent(
    uiState: LookupUiState,
    onImportClick: () -> Unit,
    onRunDuplicateAddressAudit: () -> Unit,
    onExportDuplicateAddressAuditCsv: () -> Unit,
    initialPassengerName: String? = null,
    onInitialPassengerNameConsumed: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedPassengerName by remember { mutableStateOf<String?>(null) }
    var menuExpanded by remember { mutableStateOf(false) }
    var sortMode by remember { mutableStateOf(LookupSortMode.NAME) }

    val summaryListState = rememberSaveable(saver = LazyListState.Saver) {
        LazyListState(0, 0)
    }

    LaunchedEffect(initialPassengerName) {
        initialPassengerName
            ?.takeIf { it.isNotBlank() }
            ?.let { passengerName ->

                val normalized = normalizePassengerNameForLookup(passengerName)

                searchQuery = normalized
                selectedPassengerName = normalized

                onInitialPassengerNameConsumed()
            }
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

    val selectedDetail = remember(uiState.rows, selectedPassengerName) {
        selectedPassengerName?.let { passengerName ->
            buildLookupPassengerDetail(uiState.rows, passengerName)
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

    VtsDirectoryScreenShell(
        title = "Passenger Lookup",
        showingDetail = selectedPassengerName != null,
        onBackFromDetail = { selectedPassengerName = null },
        searchValue = searchQuery,
        onSearchValueChange = {
            searchQuery = it
            selectedPassengerName = null
        },
        searchPlaceholder = "Search passengers",
        sortOptions = listOf("Name", "Trips"),
        selectedSortOption = when (sortMode) {
            LookupSortMode.NAME -> "Name"
            LookupSortMode.TRIPS -> "Trips"
        },
        onSortOptionSelected = { option ->
            sortMode = when (option) {
                "Name" -> LookupSortMode.NAME
                "Trips" -> LookupSortMode.TRIPS
                else -> sortMode
            }
        },
        actionSlot = {
            VtsOverflowMenu(
                expanded = menuExpanded,
                onExpandedChange = { menuExpanded = it }
            ) {
                DropdownMenuItem(
                    text = { Text("Import") },
                    onClick = {
                        menuExpanded = false
                        onImportClick()
                    }
                )

                DropdownMenuItem(
                    text = { Text("Export Audit CSV") },
                    onClick = {
                        menuExpanded = false
                        onExportDuplicateAddressAuditCsv()
                    }
                )

                DropdownMenuItem(
                    text = { Text("Run Lookup Audit") },
                    onClick = {
                        menuExpanded = false
                        onRunDuplicateAddressAudit()
                    }
                )

                DropdownMenuItem(
                    text = { Text("Save") },
                    onClick = {
                        menuExpanded = false
                    }
                )
            }
        },
        isListEmpty = filteredSummaries.isEmpty(),
        emptyState = {
            LookupEmptyState(
                message = if (uiState.rows.isEmpty()) {
                    "No lookup data loaded yet.\nUse the menu to import a CSV."
                } else {
                    "No passengers matched your search."
                }
            )
        },
        listContent = {
            LazyColumn(
                state = summaryListState,
                verticalArrangement = Arrangement.spacedBy(VtsSpacing.xs)
            ) {
                items(
                    items = filteredSummaries,
                    key = { it.passenger }
                ) { summary ->
                    LookupSummaryCard(
                        summary = summary,
                        onClick = { selectedPassengerName = summary.passenger }
                    )
                }
            }
        },
        detailContent = {
            if (selectedDetail == null) {
                LookupEmptyState(message = "Passenger not found.")
            } else {
                LookupDetailContent(detail = selectedDetail)
            }
        }
    )
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
private fun LookupDetailContent(
    detail: LookupPassengerDetail
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            bottom = VtsSpacing.fabClearance
        ),
        verticalArrangement = Arrangement.spacedBy(VtsSpacing.md)
    ) {
        stickyHeader {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(
                        top = VtsSpacing.sm,
                        bottom = VtsSpacing.xs
                    )
            ) {
                VtsDirectoryDetailCard(
                    title = detail.passenger,
                    showDivider = false
                ) {
                    VtsInfoRow(
                        label = "Phone",
                        value = detail.phone.orEmpty()
                    )
                }
            }
        }

        items(detail.dayGroups) { dayGroup ->
            LookupTripDateCard(
                date = dayGroup.driveDate.orEmpty(),
                trips = dayGroup.trips
            )
        }
    }
}
@Composable
private fun LookupTripDateCard(
    date: String,
    trips: List<LookupTripDetail>
) {
    VtsCard {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = date,
                style = MaterialTheme.typography.titleMedium,
                color = VtsGreen,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(VtsSpacing.md))

            trips.forEachIndexed { index, trip ->
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

                if (index < trips.lastIndex) {
                    Spacer(modifier = Modifier.height(VtsSpacing.xs))
                }
            }
        }
    }
}
@Composable
fun LookupLabelValueRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .width(80.dp)
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
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

private enum class LookupSortMode {
    NAME,
    TRIPS
}

private const val LOOKUP_PREFS = "lookup_prefs"
private const val KEY_LOOKUP_URI = "lookup_uri"

private fun saveLookupUri(context: Context, uri: Uri) {
    context.getSharedPreferences(LOOKUP_PREFS, Context.MODE_PRIVATE)
        .edit {
            putString(KEY_LOOKUP_URI, uri.toString())
        }
}

