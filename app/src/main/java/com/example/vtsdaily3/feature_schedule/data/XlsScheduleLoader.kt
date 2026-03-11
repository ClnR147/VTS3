package com.example.vtsdaily3.feature_schedule.data

import com.example.vtsdaily3.model.Trip
import java.time.LocalDate

interface XlsScheduleLoader {

    suspend fun getAvailableDates(): List<LocalDate>

    suspend fun loadTrips(date: LocalDate): List<Trip>
}