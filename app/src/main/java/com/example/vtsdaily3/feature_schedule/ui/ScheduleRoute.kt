package com.example.vtsdaily3.feature_schedule.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.vtsdaily3.model.TripStatus
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ScheduleRoute(
    viewModel: ScheduleViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState = viewModel.uiState.collectAsState().value

    ScheduleScreen(
        uiState = uiState,
        onSelectDate = viewModel::selectDate,
        onSelectViewMode = viewModel::selectViewMode,
        onMarkTripStatus = viewModel::markTripStatus,
        onReinstateTrip = viewModel::reinstateTrip,
        onRefresh = viewModel::refreshCurrentDate,
        onPreviousDate = viewModel::goToPreviousAvailableDate,
        onNextDate = viewModel::goToNextAvailableDate
    )
}