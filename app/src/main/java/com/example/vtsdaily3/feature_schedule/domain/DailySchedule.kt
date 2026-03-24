package com.example.vtsdaily3.feature_schedule.domain

import com.example.vtsdaily3.model.Trip
import com.example.vtsdaily3.model.TripId
import java.time.LocalDate

data class DailySchedule(
    val date: LocalDate,
    val availableDates: List<LocalDate>,
    val trips: List<Trip>
)

