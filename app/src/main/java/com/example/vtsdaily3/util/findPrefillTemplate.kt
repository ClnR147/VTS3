package com.example.vtsdaily3.util

import com.example.vtsdaily3.feature_lookup.data.LookupRow
import com.example.vtsdaily3.feature_lookup.util.normalizePassengerNameForLookup
import com.example.vtsdaily3.model.Trip
import com.example.vtsdaily3.model.TripId
import com.example.vtsdaily3.model.TripStatus
import java.time.LocalDate

fun findPrefillTemplate(
    rows: List<LookupRow>,
    passenger: String,
    type: String, // "PA" or "PR"
    date: LocalDate
): Trip? {

    val normalizedTarget = normalizePassengerNameForLookup(passenger)

    val match = rows
        .asReversed() // most recent first
        .firstOrNull { row ->
            val rowName = row.passenger
                ?.let(::normalizePassengerNameForLookup)
                .orEmpty()

            val matchesName = rowName == normalizedTarget

            val matchesType = when (type) {
                "PA" -> row.driveDate?.contains("PA", ignoreCase = true) == true
                "PR" -> row.driveDate?.contains("PR", ignoreCase = true) == true
                else -> false
            }

            matchesName && matchesType
        }

    return match?.let {
        Trip(
            id = TripId.stable(date, it.passenger.orEmpty(), "", it.pAddress.orEmpty()),
            date = date,
            time = "",
            name = it.passenger.orEmpty(),
            phone = it.phone.orEmpty(),
            fromAddress = it.pAddress.orEmpty(),
            toAddress = it.dAddress.orEmpty(),
            status = TripStatus.ACTIVE
        )
    }
}