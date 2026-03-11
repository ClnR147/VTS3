package com.example.vtsdaily3.feature_schedule.data

import com.example.vtsdaily3.model.TripId
import com.example.vtsdaily3.model.TripStatus
import java.time.LocalDate

interface TripStatusStore {

    suspend fun loadStatuses(date: LocalDate): List<TripStatusRecord>

    suspend fun setStatus(
        date: LocalDate,
        tripId: TripId,
        status: TripStatus
    )

    suspend fun clearStatus(
        date: LocalDate,
        tripId: TripId
    )
}