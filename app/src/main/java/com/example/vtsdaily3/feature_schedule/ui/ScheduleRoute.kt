package com.example.vtsdaily3.feature_schedule.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.vtsdaily3.app.setup.FolderPrefs
import com.example.vtsdaily3.feature_schedule.data.ScheduleRepositoryImpl
import com.example.vtsdaily3.feature_schedule.data.TripStatusStoreImpl
import com.example.vtsdaily3.feature_schedule.data.XlsScheduleLoaderImpl

@Composable
fun ScheduleRoute() {
    val context = LocalContext.current
    val folderUri = FolderPrefs.getScheduleFolderUri(context)

    if (folderUri == null) {
        Text("No schedule folder selected")
        return
    }

    val viewModel = ScheduleViewModel(
        repository = ScheduleRepositoryImpl(
            loader = XlsScheduleLoaderImpl(
                context = context,
                folderUri = folderUri
            ),
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