package com.example.vtsdaily3.feature_lookup.domain

import com.example.vtsdaily3.feature_lookup.data.LookupRow

data class LookupSummary(
    val passenger: String,
    val tripCount: Int
)

data class LookupTripDetail(
    val pickup: String = "",
    val dropoff: String = ""
)

data class LookupTripDayGroup(
    val driveDate: String? = null,
    val trips: List<LookupTripDetail> = emptyList()
)

data class LookupPassengerDetail(
    val passenger: String,
    val phone: String? = null,
    val dayGroups: List<LookupTripDayGroup> = emptyList()
)

fun buildLookupSummaries(rows: List<LookupRow>): List<LookupSummary> {
    return rows
        .mapNotNull { row ->
            val passenger = row.passenger?.trim()
            if (passenger.isNullOrBlank()) null else passenger
        }
        .groupBy { it }
        .map { (passenger, passengerRows) ->
            LookupSummary(
                passenger = passenger,
                tripCount = passengerRows.size
            )
        }
        .sortedBy { it.passenger }
}

fun buildLookupPassengerDetail(
    rows: List<LookupRow>,
    passengerName: String
): LookupPassengerDetail? {
    val targetName = passengerName.trim()

    val matches = rows.filter { row ->
        row.passenger?.trim() == targetName
    }

    if (matches.isEmpty()) return null

    val phone = matches
        .firstOrNull { !it.phone.isNullOrBlank() }
        ?.phone
        ?.trim()

    val dayGroups = matches
        .groupBy { it.driveDate }
        .map { (driveDate, dayRows) ->
            LookupTripDayGroup(
                driveDate = driveDate,
                trips = dayRows.map { row ->
                    LookupTripDetail(
                        pickup = row.pAddress?.trim().orEmpty(),
                        dropoff = row.dAddress?.trim().orEmpty()
                    )
                }
            )
        }
        .sortedBy { it.driveDate ?: "" }

    return LookupPassengerDetail(
        passenger = targetName,
        phone = phone,
        dayGroups = dayGroups
    )
}