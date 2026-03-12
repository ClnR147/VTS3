package com.example.vtsdaily3.feature_schedule.ui.state

import com.example.vtsdaily3.feature_schedule.domain.DailySchedule
import com.example.vtsdaily3.model.TripStatus
import com.example.vtsdaily3.model.TripViewMode

object ScheduleUiMapper {

    fun map(
        dailySchedule: DailySchedule,
        selectedViewMode: TripViewMode,
        isLoading: Boolean,
        errorMessage: String?
    ): ScheduleUiState {

        val activeTrips = dailySchedule.trips.filter { it.status == TripStatus.ACTIVE }
        val completedTrips = dailySchedule.trips.filter { it.status == TripStatus.COMPLETED }
        val otherTrips = dailySchedule.trips.filter {
            it.status != TripStatus.ACTIVE && it.status != TripStatus.COMPLETED
        }

        val tripsForSelectedView = when (selectedViewMode) {
            TripViewMode.ACTIVE -> activeTrips
            TripViewMode.COMPLETED -> completedTrips
            TripViewMode.OTHER -> otherTrips
        }

        return ScheduleUiState(
            selectedDate = dailySchedule.date,
            availableDates = dailySchedule.availableDates,
            selectedViewMode = selectedViewMode,
            isLoading = isLoading,
            tripsForSelectedView = tripsForSelectedView,
            activeCount = activeTrips.size,
            completedCount = completedTrips.size,
            otherCount = otherTrips.size,
            errorMessage = errorMessage
        )
    }
}