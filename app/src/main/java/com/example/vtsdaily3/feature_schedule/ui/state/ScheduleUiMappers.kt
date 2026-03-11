package com.example.vtsdaily3.feature_schedule.ui.state

import com.example.vtsdaily3.feature_schedule.domain.DailySchedule
import com.example.vtsdaily3.model.Trip
import com.example.vtsdaily3.model.TripStatus
import com.example.vtsdaily3.model.TripViewMode

fun DailySchedule.toUiState(
    viewMode: TripViewMode
): ScheduleUiState {

    val activeCount = trips.count { it.status == TripStatus.ACTIVE }
    val completedCount = trips.count { it.status == TripStatus.COMPLETED }
    val otherCount = trips.count {
        it.status == TripStatus.REMOVED ||
                it.status == TripStatus.CANCELLED ||
                it.status == TripStatus.NOSHOW
    }

    return ScheduleUiState(
        isLoading = false,
        selectedDate = selectedDate,
        availableDates = availableDates,
        selectedViewMode = viewMode,
        allTripsForDate = trips,
        visibleTrips = trips.filterByViewMode(viewMode),
        activeCount = activeCount,
        completedCount = completedCount,
        otherCount = otherCount,
        errorMessage = null
    )
}

private fun List<Trip>.filterByViewMode(
    mode: TripViewMode
): List<Trip> {
    return when (mode) {
        TripViewMode.ACTIVE ->
            filter { it.status == TripStatus.ACTIVE }

        TripViewMode.COMPLETED ->
            filter { it.status == TripStatus.COMPLETED }

        TripViewMode.OTHER ->
            filter {
                it.status == TripStatus.REMOVED ||
                        it.status == TripStatus.CANCELLED ||
                        it.status == TripStatus.NOSHOW
            }
    }
}