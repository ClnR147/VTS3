package com.example.vtsdaily3.feature_lookup.data

import android.util.Log

data class InsertTripPrefill(
    val phone: String,
    val pickupAddress: String,
    val dropoffAddress: String
)

fun findInsertTripPrefill(
    rows: List<LookupRow>,
    passengerName: String,
    tripType: String
): InsertTripPrefill? {
    val lookupTripType = when (tripType) {
        "PA" -> "appt"
        "PR" -> "return"
        else -> tripType
    }

    return rows
        .asReversed()
        .firstOrNull { row ->
            row.passenger == passengerName &&
                    row.tripType == lookupTripType
        }
        ?.let { row ->
            InsertTripPrefill(
                phone = row.phone.orEmpty(),
                pickupAddress = row.pAddress.orEmpty(),
                dropoffAddress = row.dAddress.orEmpty()
            )
        }
}