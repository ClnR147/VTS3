package com.example.vtsdaily3.feature_schedule.ui.state

import com.example.vtsdaily3.feature_schedule.domain.DailySchedule
import com.example.vtsdaily3.model.TripStatus
import com.example.vtsdaily3.model.TripViewMode
import org.apache.poi.hssf.usermodel.HeaderFooter.date

fun DailySchedule.toUiState(
    selectedViewMode: TripViewMode
): ScheduleUiState {
    val activeTrips = trips.filter { it.status == TripStatus.ACTIVE }
    val completedTrips = trips.filter { it.status == TripStatus.COMPLETED }
    val otherTrips = trips.filter {
        it.status == TripStatus.REMOVED ||
                it.status == TripStatus.CANCELLED ||
                it.status == TripStatus.NOSHOW
    }

    val visibleTrips = when (selectedViewMode) {
        TripViewMode.ACTIVE -> activeTrips
        TripViewMode.COMPLETED -> completedTrips
        TripViewMode.OTHER -> otherTrips
    }

        return ScheduleUiState(
            selectedDate = date,
            selectedViewMode = selectedViewMode,
            isLoading = false,
            tripsForSelectedView = visibleTrips,
            activeCount = activeTrips.size,
            completedCount = completedTrips.size,
            otherCount = otherTrips.size,
            errorMessage = null
        )
}