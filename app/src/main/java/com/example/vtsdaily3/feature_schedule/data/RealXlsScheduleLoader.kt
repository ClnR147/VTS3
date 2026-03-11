package com.example.vtsdaily3.feature_schedule.data

import com.example.vtsdaily3.model.Trip
import java.time.LocalDate

class RealXlsScheduleLoader(
    private val fileCatalog: ScheduleFileCatalog,
    private val tripParser: XlsTripParser
) : XlsScheduleLoader {

    override suspend fun getAvailableDates(): List<LocalDate> {
        return fileCatalog
            .getAvailableScheduleFiles()
            .map { it.date }
            .distinct()
            .sorted()
    }

    override suspend fun loadTrips(date: LocalDate): List<Trip> {
        val fileRef = fileCatalog.findScheduleFile(date) ?: return emptyList()
        return tripParser.parse(fileRef)
    }
}