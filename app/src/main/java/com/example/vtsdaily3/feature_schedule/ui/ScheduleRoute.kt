package com.example.vtsdaily3.feature_schedule.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vtsdaily3.feature_schedule.data.FakeScheduleFileCatalog
import com.example.vtsdaily3.feature_schedule.data.FakeXlsScheduleLoader
import com.example.vtsdaily3.feature_schedule.data.FakeXlsTripParser
import com.example.vtsdaily3.feature_schedule.data.InMemoryTripStatusStore
import com.example.vtsdaily3.feature_schedule.data.RealXlsScheduleLoader
import com.example.vtsdaily3.feature_schedule.data.ScheduleRepositoryImpl
import com.example.vtsdaily3.feature_schedule.data.XlsScheduleLoader
import com.example.vtsdaily3.feature_schedule.data.XlsScheduleLoaderImpl

@Composable
fun ScheduleRoute() {
    val context = LocalContext.current

    val repository = remember {
        ScheduleRepositoryImpl(
            loader = RealXlsScheduleLoader(
                fileCatalog = FakeScheduleFileCatalog(),
                tripParser = FakeXlsTripParser()
            ),
            statusStore = InMemoryTripStatusStore()
        )
    }

    val viewModel: ScheduleViewModel = viewModel(
        factory = ScheduleViewModelFactory(repository)
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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