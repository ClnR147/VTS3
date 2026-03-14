package com.example.vtsdaily3.ui.preview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.vtsdaily3.feature_lookup.ui.LookupScreen
import com.example.vtsdaily3.feature_schedule.ui.ScheduleScreen
import com.example.vtsdaily3.feature_schedule.ui.state.ScheduleUiState
import com.example.vtsdaily3.model.TripViewMode
import java.time.LocalDate

@Preview(
    showBackground = true,
    widthDp = 800,
    heightDp = 900
)
@Composable
fun LookupVsSchedulePreview() {
    Row(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            LookupScreen()
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            ScheduleScreenPreview()
        }
    }
}

@Composable
private fun ScheduleScreenPreview() {
    val previewState = ScheduleUiState(
        selectedDate = LocalDate.of(2026, 3, 18),
        selectedViewMode = TripViewMode.ACTIVE,
        isLoading = false,
        tripsForSelectedView = emptyList(),
        activeCount = 5,
        completedCount = 2,
        otherCount = 1,
        errorMessage = null
    )

    ScheduleScreen(
        uiState = previewState,
        onSelectDate = {},
        onSelectViewMode = {},
        onMarkTripStatus = { _, _ -> },
        onReinstateTrip = {},
        onRefresh = {},
        onPreviousDate = {},
        onNextDate = {}
    )
}