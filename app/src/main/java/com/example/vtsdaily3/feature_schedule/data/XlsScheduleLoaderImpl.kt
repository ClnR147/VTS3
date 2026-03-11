package com.example.vtsdaily3.feature_schedule.data

import com.example.vtsdaily3.model.Trip
import java.time.LocalDate

class XlsScheduleLoaderImpl : XlsScheduleLoader {

    override suspend fun getAvailableDates(): List<LocalDate> {
        return emptyList()
    }

    override suspend fun loadTrips(date: LocalDate): List<Trip> {
        return emptyList()
    }
}