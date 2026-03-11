package com.example.vtsdaily3.feature_schedule.ui.state

import com.example.vtsdaily3.feature_schedule.domain.DailySchedule
import com.example.vtsdaily3.model.Trip
import com.example.vtsdaily3.model.TripStatus
import com.example.vtsdaily3.model.TripViewMode

object ScheduleUiMapper {

    fun map(
        dailySchedule: DailySchedule,
        selectedViewMode: TripViewMode,
        isLoading: Boolean = false,
        errorMessage: String? = null
    ): ScheduleUiState {
        val allTrips = dailySchedule.trips
        val sortedDates = dailySchedule.availableDates.sorted()
        val selectedDate = dailySchedule.date

        val filteredTrips = allTrips.filterByViewMode(selectedViewMode)

        val currentIndex = sortedDates.indexOf(selectedDate)
        val canGoToPreviousDate = currentIndex > 0
        val canGoToNextDate = currentIndex >= 0 && currentIndex < sortedDates.lastIndex

        return ScheduleUiState(
            selectedDate = selectedDate,
            selectedViewMode = selectedViewMode,
            isLoading = isLoading,
            tripsForSelectedView = filteredTrips,
            activeCount = allTrips.count { it.status == TripStatus.ACTIVE },
            completedCount = allTrips.count { it.status == TripStatus.COMPLETED },
            otherCount = allTrips.count {
                it.status == TripStatus.REMOVED ||
                        it.status == TripStatus.CANCELLED ||
                        it.status == TripStatus.NOSHOW
            },
            canGoToPreviousDate = canGoToPreviousDate,
            canGoToNextDate = canGoToNextDate,
            errorMessage = errorMessage
        )
    }

    private fun List<Trip>.filterByViewMode(viewMode: TripViewMode): List<Trip> {
        return when (viewMode) {
            TripViewMode.ACTIVE -> filter { it.status == TripStatus.ACTIVE }
            TripViewMode.COMPLETED -> filter { it.status == TripStatus.COMPLETED }
            TripViewMode.OTHER -> filter {
                it.status == TripStatus.REMOVED ||
                        it.status == TripStatus.CANCELLED ||
                        it.status == TripStatus.NOSHOW
            }
        }
    }
}