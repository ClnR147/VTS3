package com.example.vtsdaily3.feature_schedule.domain

import com.example.vtsdaily3.model.Trip
import java.time.LocalDate

data class DailySchedule(
    val selectedDate: LocalDate,
    val availableDates: List<LocalDate>,
    val trips: List<Trip>
)