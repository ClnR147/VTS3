package com.example.vtsdaily3.feature_schedule.data

import com.example.vtsdaily3.model.TripId
import com.example.vtsdaily3.model.TripStatus
import java.time.LocalDate

class TripStatusStoreImpl : TripStatusStore {

    override suspend fun loadStatuses(date: LocalDate): List<TripStatusRecord> {
        return emptyList()
    }

    override suspend fun setStatus(
        date: LocalDate,
        tripId: TripId,
        status: TripStatus
    ) {
        // no-op for now
    }

    override suspend fun clearStatus(
        date: LocalDate,
        tripId: TripId
    ) {
        // no-op for now
    }
}