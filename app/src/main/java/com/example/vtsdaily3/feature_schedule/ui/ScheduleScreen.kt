package com.example.vtsdaily3.feature_schedule.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.vtsdaily3.feature_schedule.ui.state.ScheduleUiState
import com.example.vtsdaily3.model.TripId
import com.example.vtsdaily3.model.TripStatus
import com.example.vtsdaily3.model.TripViewMode
import java.time.LocalDate

@Composable
fun ScheduleScreen(
    uiState: ScheduleUiState,
    onSelectDate: (LocalDate) -> Unit,
    onSelectViewMode: (TripViewMode) -> Unit,
    onMarkTripStatus: (TripId, TripStatus) -> Unit,
    onReinstateTrip: (TripId) -> Unit,
    onRefresh: () -> Unit
) {
    Text("Schedule Screen")
}