package com.example.vtsdaily3.feature_schedule.ui

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import com.example.vtsdaily3.feature_schedule.di.ScheduleModule

@Composable
fun ScheduleRoute() {
    val context = LocalContext.current
    val folderPrefs = remember { ScheduleModule.createFolderPrefs(context) }

    var savedFolderUri by remember {
        mutableStateOf(folderPrefs.getFolderUri())
    }

    val factory = remember(savedFolderUri) {
        ScheduleModule.createViewModelFactory(context)
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
            savedFolderUri = uri.toString()
        }
    }

    LaunchedEffect(savedFolderUri) {
        if (savedFolderUri.isNullOrBlank()) {
            folderPickerLauncher.launch(null)
        }
    }

    val viewModel: ScheduleViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when {
        savedFolderUri.isNullOrBlank() -> {
            ScheduleRouteMessage(
                message = "Please select your PassengerSchedules folder."
            )
        }

        !uiState.isLoading && uiState.availableDates.isEmpty() -> {
            ScheduleRouteMessage(
                message = "No valid .xls schedule files were found in the selected folder.",
                buttonLabel = "Pick Schedule Folder Again",
                onButtonClick = { folderPickerLauncher.launch(null) }
            )
        }

        else -> {
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
}

@Composable
private fun ScheduleRouteMessage(
    message: String,
    buttonLabel: String? = null,
    onButtonClick: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = message)

        if (buttonLabel != null && onButtonClick != null) {
            Button(
                modifier = Modifier.padding(top = 16.dp),
                onClick = onButtonClick
            ) {
                Text(buttonLabel)
            }
        }
    }
}