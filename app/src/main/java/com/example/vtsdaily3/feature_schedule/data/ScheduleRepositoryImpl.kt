package com.example.vtsdaily3.feature_schedule.data

import com.example.vtsdaily3.feature_schedule.domain.DailySchedule
import com.example.vtsdaily3.model.TripId
import com.example.vtsdaily3.model.TripStatus
import java.time.LocalDate

class ScheduleRepositoryImpl(
    private val loader: XlsScheduleLoader,
    private val statusStore: TripStatusStore
) : ScheduleRepository {

    override suspend fun getAvailableDates(): List<LocalDate> {
        return loader.getAvailableDates()
    }

    override suspend fun loadSchedule(date: LocalDate): DailySchedule {
        val availableDates = loader.getAvailableDates()
        val baseTrips = loader.loadTrips(date)
        val statuses = statusStore.loadStatuses(date)

        val statusMap = statuses.associate { TripId(it.tripId) to it.status }

        val mergedTrips = baseTrips.map { trip ->
            val savedStatus = statusMap[trip.id] ?: TripStatus.ACTIVE
            trip.copy(status = savedStatus)
        }

        return DailySchedule(
            date = date,
            trips = mergedTrips
        )
    }

    override suspend fun setTripStatus(
        date: LocalDate,
        tripId: TripId,
        status: TripStatus
    ) {
        statusStore.setStatus(date, tripId, status)
    }

    override suspend fun clearTripStatus(
        date: LocalDate,
        tripId: TripId
    ) {
        statusStore.clearStatus(date, tripId)
    }
}