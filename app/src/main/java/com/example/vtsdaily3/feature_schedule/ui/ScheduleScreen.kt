package com.example.vtsdaily3.feature_schedule.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.vtsdaily3.feature_schedule.ui.state.ScheduleUiState
import com.example.vtsdaily3.model.Trip
import com.example.vtsdaily3.model.TripId
import com.example.vtsdaily3.model.TripStatus
import com.example.vtsdaily3.model.TripViewMode
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun ScheduleScreen(
    uiState: ScheduleUiState,
    onSelectDate: (LocalDate) -> Unit,
    onSelectViewMode: (TripViewMode) -> Unit,
    onMarkTripStatus: (TripId, TripStatus) -> Unit,
    onReinstateTrip: (TripId) -> Unit,
    onRefresh: () -> Unit,
    onPreviousDate: () -> Unit,
    onNextDate: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        ScheduleDateHeader(
            selectedDate = uiState.selectedDate,
            onPreviousClick = onPreviousDate,
            onNextClick = onNextDate
        )

        ScheduleTabs(
            selectedViewMode = uiState.selectedViewMode,
            activeCount = uiState.activeCount,
            completedCount = uiState.completedCount,
            otherCount = uiState.otherCount,
            onTabSelected = onSelectViewMode
        )

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.tripsForSelectedView.isEmpty() -> {
                EmptyScheduleState(
                    viewMode = uiState.selectedViewMode,
                    modifier = Modifier.fillMaxSize()
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = uiState.tripsForSelectedView,
                        key = { trip -> trip.id.toString() }
                    ) { trip ->
                        TripCard(
                            trip = trip,
                            viewMode = uiState.selectedViewMode,
                            onTripActionSelected = { menuAction ->
                                when (menuAction) {
                                    TripMenuAction.COMPLETE -> {
                                        onMarkTripStatus(trip.id, TripStatus.COMPLETED)
                                    }
                                    TripMenuAction.NOSHOW -> {
                                        onMarkTripStatus(trip.id, TripStatus.NOSHOW)
                                    }
                                    TripMenuAction.CANCEL -> {
                                        onMarkTripStatus(trip.id, TripStatus.CANCELLED)
                                    }
                                    TripMenuAction.REMOVE -> {
                                        onMarkTripStatus(trip.id, TripStatus.REMOVED)
                                    }
                                    TripMenuAction.REINSTATE -> {
                                        onReinstateTrip(trip.id)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduleDateHeader(
    selectedDate: LocalDate,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formatter = remember { DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy") }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousClick) {
            Icon(
                imageVector = Icons.Filled.ChevronLeft,
                contentDescription = "Previous day"
            )
        }

        Text(
            text = selectedDate.format(formatter),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        IconButton(onClick = onNextClick) {
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = "Next day"
            )
        }
    }
}

@Composable
private fun ScheduleTabs(
    selectedViewMode: TripViewMode,
    activeCount: Int,
    completedCount: Int,
    otherCount: Int,
    onTabSelected: (TripViewMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = listOf(
        Triple(TripViewMode.ACTIVE, "Active", activeCount),
        Triple(TripViewMode.COMPLETED, "Completed", completedCount),
        Triple(TripViewMode.OTHER, "Other", otherCount)
    )

    val selectedIndex = tabs.indexOfFirst { it.first == selectedViewMode }.coerceAtLeast(0)

    TabRow(
        selectedTabIndex = selectedIndex,
        modifier = modifier.fillMaxWidth()
    ) {
        tabs.forEachIndexed { index, (mode, label, count) ->
            Tab(
                selected = index == selectedIndex,
                onClick = { onTabSelected(mode) },
                text = {
                    Text("$label ($count)")
                }
            )
        }
    }
}

@Composable
private fun TripCard(
    trip: Trip,
    viewMode: TripViewMode,
    onTripActionSelected: (TripMenuAction) -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = trip.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = trip.time,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Box {
                    IconButton(
                        onClick = { menuExpanded = true }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = "Trip actions"
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        availableActionsFor(trip.status).forEach { action ->
                            DropdownMenuItem(
                                text = { Text(action.label) },
                                onClick = {
                                    menuExpanded = false
                                    onTripActionSelected(action)
                                }
                            )
                        }
                    }
                }
            }

            if (trip.phone.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = trip.phone,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = "From: ${trip.fromAddress}",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "To: ${trip.toAddress}",
                style = MaterialTheme.typography.bodyMedium
            )

            if (viewMode == TripViewMode.OTHER) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = statusLabel(trip.status),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun EmptyScheduleState(
    viewMode: TripViewMode,
    modifier: Modifier = Modifier
) {
    val message = when (viewMode) {
        TripViewMode.ACTIVE -> "No active trips for this date."
        TripViewMode.COMPLETED -> "No completed trips for this date."
        TripViewMode.OTHER -> "No other trips for this date."
    }

    Box(
        modifier = modifier.padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

private fun availableActionsFor(status: TripStatus): List<TripMenuAction> {
    return when (status) {
        TripStatus.ACTIVE -> listOf(
            TripMenuAction.COMPLETE,
            TripMenuAction.CANCEL,
            TripMenuAction.NOSHOW,
            TripMenuAction.REMOVE
        )

        TripStatus.COMPLETED -> listOf(
            TripMenuAction.REINSTATE,
            TripMenuAction.REMOVE
        )

        TripStatus.CANCELLED,
        TripStatus.NOSHOW,
        TripStatus.REMOVED -> listOf(
            TripMenuAction.REINSTATE
        )
    }
}

private fun statusLabel(status: TripStatus): String {
    return when (status) {
        TripStatus.ACTIVE -> "Active"
        TripStatus.COMPLETED -> "Completed"
        TripStatus.REMOVED -> "Removed"
        TripStatus.CANCELLED -> "Cancelled"
        TripStatus.NOSHOW -> "No Show"
    }
}

sealed interface ScheduleUiAction {
    data object PreviousDayClicked : ScheduleUiAction
    data object NextDayClicked : ScheduleUiAction
    data class ViewModeSelected(val mode: TripViewMode) : ScheduleUiAction
    data class TripActionSelected(
        val tripId: TripId,
        val action: TripMenuAction
    ) : ScheduleUiAction
}

enum class TripMenuAction(val label: String) {
    COMPLETE("Complete"),
    CANCEL("Cancel"),
    NOSHOW("No Show"),
    REMOVE("Remove"),
    REINSTATE("Reinstate")
}

@Preview(showBackground = true)
@Composable
private fun ScheduleScreenPreviewActive() {
    ScheduleScreen(
        uiState = previewState(
            mode = TripViewMode.ACTIVE,
            trips = listOf(
                previewTrip(
                    name = "John Smith",
                    time = "08:30 AM",
                    status = TripStatus.ACTIVE
                ),
                previewTrip(
                    name = "Mary Jones",
                    time = "09:15 AM",
                    status = TripStatus.ACTIVE
                )
            )
        ),
        onSelectDate = {},
        onSelectViewMode = {},
        onMarkTripStatus = { _, _ -> },
        onReinstateTrip = {},
        onRefresh = {},
        onPreviousDate = {},
        onNextDate = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun ScheduleScreenPreviewCompleted() {
    ScheduleScreen(
        uiState = previewState(
            mode = TripViewMode.COMPLETED,
            trips = listOf(
                previewTrip(
                    name = "Alice Brown",
                    time = "10:00 AM",
                    status = TripStatus.COMPLETED
                )
            )
        ),
        onSelectDate = {},
        onSelectViewMode = {},
        onMarkTripStatus = { _, _ -> },
        onReinstateTrip = {},
        onRefresh = {},
        onPreviousDate = {},
        onNextDate = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun ScheduleScreenPreviewOther() {
    ScheduleScreen(
        uiState = previewState(
            mode = TripViewMode.OTHER,
            trips = listOf(
                previewTrip(
                    name = "Robert White",
                    time = "11:45 AM",
                    status = TripStatus.CANCELLED
                ),
                previewTrip(
                    name = "Nancy Green",
                    time = "01:15 PM",
                    status = TripStatus.NOSHOW
                )
            )
        ),
        onSelectDate = {},
        onSelectViewMode = {},
        onMarkTripStatus = { _, _ -> },
        onReinstateTrip = {},
        onRefresh = {},
        onPreviousDate = {},
        onNextDate = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun ScheduleScreenPreviewEmpty() {
    ScheduleScreen(
        uiState = previewState(
            mode = TripViewMode.ACTIVE,
            trips = emptyList()
        ),
        onSelectDate = {},
        onSelectViewMode = {},
        onMarkTripStatus = { _, _ -> },
        onReinstateTrip = {},
        onRefresh = {},
        onPreviousDate = {},
        onNextDate = {}
    )
}

private fun previewState(
    mode: TripViewMode,
    trips: List<Trip>
): ScheduleUiState {
    return ScheduleUiState(
        selectedDate = LocalDate.of(2026, 3, 10),
        selectedViewMode = mode,
        isLoading = false,
        tripsForSelectedView = trips,
        activeCount = if (mode == TripViewMode.ACTIVE) trips.size else 2,
        completedCount = if (mode == TripViewMode.COMPLETED) trips.size else 1,
        otherCount = if (mode == TripViewMode.OTHER) trips.size else 2
      )
}

private fun previewTrip(
    name: String,
    time: String,
    status: TripStatus
): Trip {
    return Trip(
        id = TripId("preview-$name-$time"),
        date = LocalDate.of(2026, 3, 10),
        time = time,
        name = name,
        phone = "555-123-4567",
        fromAddress = "123 Main St",
        toAddress = "456 Oak Ave",
        status = status
    )
}