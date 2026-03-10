package com.example.vtsdaily3.feature_schedule.data

import com.example.vtsdaily3.feature_schedule.domain.DailySchedule
import com.example.vtsdaily3.model.TripId
import com.example.vtsdaily3.model.TripStatus
import java.time.LocalDate


interface ScheduleRepository {

    suspend fun getAvailableDates(): List<LocalDate>

    suspend fun loadSchedule(date: LocalDate): DailySchedule

    suspend fun setTripStatus(
        date: LocalDate,
        tripId: TripId,
        status: TripStatus
    )

    suspend fun clearTripStatus(
        date: LocalDate,
        tripId: TripId
    )
}
