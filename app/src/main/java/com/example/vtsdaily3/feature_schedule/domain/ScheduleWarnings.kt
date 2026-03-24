package com.example.vtsdaily3.feature_schedule.domain


import com.example.vtsdaily3.feature_clinics.data.ClinicEntry
import com.example.vtsdaily3.feature_clinics.domain.findMatchingClinic
import com.example.vtsdaily3.model.Trip
import com.example.vtsdaily3.model.TripId

data class ScheduleWarning(
    val tripId: TripId,
    val message: String
)

fun buildScheduleWarnings(
    trips: List<Trip>,
    clinics: List<ClinicEntry>
): List<ScheduleWarning> {
    val warnings = mutableListOf<ScheduleWarning>()

    trips.forEach { trip ->
        val time = trip.time.trim()
        val isPA = time.contains("PA", ignoreCase = true)
        val isPR = time.contains("PR", ignoreCase = true)

        if (!isPA && !isPR) {
            warnings += ScheduleWarning(
                tripId = trip.id,
                message = "Missing PA/PR"
            )
        }

        if (isPA) {
            val clinic = findMatchingClinic(
                address = trip.toAddress,
                clinics = clinics
            )
            if (clinic == null) {
                warnings += ScheduleWarning(
                    tripId = trip.id,
                    message = "PA trip but TO is not a known clinic"
                )
            }
        }

        if (isPR) {
            val clinic = findMatchingClinic(
                address = trip.fromAddress,
                clinics = clinics
            )
            if (clinic == null) {
                warnings += ScheduleWarning(
                    tripId = trip.id,
                    message = "PR trip but FROM is not a known clinic"
                )
            }
        }

        if (trip.phone.trim().isBlank()) {
            warnings += ScheduleWarning(
                tripId = trip.id,
                message = "Missing passenger phone"
            )
        }
    }

    return warnings
}