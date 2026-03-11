package com.example.vtsdaily3.feature_schedule.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import com.example.vtsdaily3.feature_schedule.data.ScheduleRepositoryImpl
import com.example.vtsdaily3.feature_schedule.data.TripStatusStoreImpl
import com.example.vtsdaily3.feature_schedule.data.XlsScheduleLoaderImpl

@Composable
fun ScheduleRoute() {
    val viewModel = ScheduleViewModel(
        repository = ScheduleRepositoryImpl(
            loader = XlsScheduleLoaderImpl(),
            statusStore = TripStatusStoreImpl()
        )
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadInitial()
    }

    ScheduleScreen(
        uiState = uiState,
        onSelectDate = viewModel::selectDate,
        onSelectViewMode = viewModel::selectViewMode,
        onMarkTripStatus = viewModel::markTripStatus,
        onReinstateTrip = viewModel::reinstateTrip,
        onRefresh = viewModel::refreshCurrentDate
    )
}