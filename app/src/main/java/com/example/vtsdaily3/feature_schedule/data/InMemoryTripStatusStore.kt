package com.example.vtsdaily3.feature_schedule.data


import com.example.vtsdaily3.model.TripId
import com.example.vtsdaily3.model.TripStatus
import java.time.LocalDate

class InMemoryTripStatusStore : TripStatusStore {

    private val store = mutableMapOf<LocalDate, MutableMap<TripId, TripStatus>>()

    override suspend fun loadStatuses(date: LocalDate): List<TripStatusRecord> {
        val dayStatuses = store[date].orEmpty()

        return dayStatuses.map { (tripId, status) ->
            TripStatusRecord(
                tripId = tripId.toString(),
                status = status
            )
        }
    }

    override suspend fun setStatus(
        date: LocalDate,
        tripId: TripId,
        status: TripStatus
    ) {
        val dayStatuses = store.getOrPut(date) { mutableMapOf() }
        dayStatuses[tripId] = status
    }

    override suspend fun clearStatus(
        date: LocalDate,
        tripId: TripId
    ) {
        store[date]?.remove(tripId)
    }
}