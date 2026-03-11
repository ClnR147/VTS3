package com.example.vtsdaily3.feature_schedule.data

import com.example.vtsdaily3.model.Trip
import com.example.vtsdaily3.model.TripId
import java.time.LocalDate

class FakeXlsTripParser : XlsTripParser {

    override suspend fun parse(fileRef: ScheduleFileRef): List<Trip> {
        val date = fileRef.date

        return listOf(
            Trip(
                id = TripId("trip-${date}-1"),
                date = date,
                time = "08:00 AM",
                name = "John Smith",
                phone = "555-1111",
                fromAddress = "101 Main St",
                toAddress = "Clinic A"
            ),
            Trip(
                id = TripId("trip-${date}-2"),
                date = date,
                time = "09:30 AM",
                name = "Mary Jones",
                phone = "555-2222",
                fromAddress = "202 Oak Ave",
                toAddress = "Hospital B"
            )
        )
    }
}