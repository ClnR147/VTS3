package com.example.vtsdaily3.feature_schedule.data

import com.example.vtsdaily3.feature_schedule.domain.DailySchedule
import com.example.vtsdaily3.model.TripId
import com.example.vtsdaily3.model.TripStatus
import java.time.LocalDate

class ScheduleRepositoryImpl(
    private val loader: XlsScheduleLoader,
    private val statusStore: TripStatusStore
) : ScheduleRepository {

    private var cachedAvailableDates: List<LocalDate>? = null

    private suspend fun getCachedAvailableDates(): List<LocalDate> {
        cachedAvailableDates?.let { return it }

        val dates = loader.getAvailableDates().sorted()
        cachedAvailableDates = dates
        return dates
    }

    override suspend fun getAvailableDates(): List<LocalDate> {
        return getCachedAvailableDates()
    }

    override suspend fun loadSchedule(date: LocalDate): DailySchedule {
        val rawTrips = try {
            loader.loadTrips(date)
        } catch (e: Exception) {
            emptyList()
        }

        val statuses = statusStore.loadStatuses(date)
        val statusMap = statuses.associate { it.tripId to it.status }

        val mergedTrips = rawTrips.map { trip ->
            trip.copy(
                status = statusMap[trip.id.value] ?: TripStatus.ACTIVE
            )
        }

        val availableDates = getCachedAvailableDates()

        return DailySchedule(
            date = date,
            availableDates = availableDates,
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

    override suspend fun refreshCatalog() {
        cachedAvailableDates = loader.getAvailableDates().sorted()
    }
}