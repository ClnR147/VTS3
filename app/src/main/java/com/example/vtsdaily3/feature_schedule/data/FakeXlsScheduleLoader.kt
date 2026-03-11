package com.example.vtsdaily3.feature_schedule.data

import com.example.vtsdaily3.model.Trip
import com.example.vtsdaily3.model.TripId
import com.example.vtsdaily3.model.TripStatus
import java.time.LocalDate

class FakeXlsScheduleLoader : XlsScheduleLoader {

    override suspend fun getAvailableDates(): List<LocalDate> {
        return listOf(
            LocalDate.now().minusDays(1),
            LocalDate.now(),
            LocalDate.now().plusDays(1)
        )
    }

    override suspend fun loadTrips(date: LocalDate): List<Trip> {
        return listOf(
            Trip(
                id = TripId("trip1"),
                date = date,
                time = "08:30 AM",
                name = "John Smith",
                phone = "555-1111",
                fromAddress = "123 Main St",
                toAddress = "Clinic A",
                status = TripStatus.ACTIVE
            ),
            Trip(
                id = TripId("trip2"),
                date = date,
                time = "09:15 AM",
                name = "Mary Jones",
                phone = "555-2222",
                fromAddress = "456 Oak Ave",
                toAddress = "Hospital B",
                status = TripStatus.ACTIVE
            )
        )
    }
}