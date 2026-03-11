package com.example.vtsdaily3.feature_schedule.ui.state

import com.example.vtsdaily3.model.Trip
import com.example.vtsdaily3.model.TripViewMode
import java.time.LocalDate

data class ScheduleUiState(

    val isLoading: Boolean = false,

    val selectedDate: LocalDate? = null,

    val availableDates: List<LocalDate> = emptyList(),

    val selectedViewMode: TripViewMode = TripViewMode.ACTIVE,

    val allTripsForDate: List<Trip> = emptyList(),

    val visibleTrips: List<Trip> = emptyList(),

    val activeCount: Int = 0,
    val completedCount: Int = 0,
    val otherCount: Int = 0,

    val errorMessage: String? = null
)