package com.example.vtsdaily3.feature_schedule.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vtsdaily3.feature_schedule.data.AndroidScheduleFileCatalog
import com.example.vtsdaily3.feature_schedule.data.PrefsScheduleFolderProvider
import com.example.vtsdaily3.feature_schedule.data.RealXlsScheduleLoader
import com.example.vtsdaily3.feature_schedule.data.ScheduleFolderPrefs
import com.example.vtsdaily3.feature_schedule.data.ScheduleRepositoryImpl
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import com.example.vtsdaily3.feature_schedule.data.JsonTripStatusStore
import com.example.vtsdaily3.feature_schedule.data.PoiXlsTripParser


@Composable
fun ScheduleRoute() {
    val context = LocalContext.current
    val appContext = context.applicationContext

    val folderPrefs = remember {
        ScheduleFolderPrefs(appContext)
    }

    val repository = remember {
        ScheduleRepositoryImpl(
            loader = RealXlsScheduleLoader(
                fileCatalog = AndroidScheduleFileCatalog(
                    context = appContext,
                    folderProvider = PrefsScheduleFolderProvider(folderPrefs)
                ),
                tripParser = PoiXlsTripParser(appContext)
            ),
            statusStore = JsonTripStatusStore(appContext)
        )
    }

    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            folderPrefs.saveFolderUri(uri.toString())
        }
    }

    val viewModel: ScheduleViewModel = viewModel(
        factory = ScheduleViewModelFactory(repository)
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column {
        Button(
            onClick = { folderPickerLauncher.launch(null) }
        ) {
            Text("Pick Schedule Folder")
        }

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
}