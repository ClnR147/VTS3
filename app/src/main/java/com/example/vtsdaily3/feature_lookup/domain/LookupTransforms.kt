package com.example.vtsdaily3.feature_lookup.domain

import com.example.vtsdaily3.feature_lookup.data.LookupRow
import com.example.vtsdaily3.feature_lookup.util.normalizePassengerNameForLookup
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class LookupSummary(
    val passengerId: String,
    val passenger: String,
    val tripCount: Int,
    val addresses: List<String>
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

private fun parseLookupDriveDate(raw: String?): LocalDate? {
    val value = raw?.trim().orEmpty()
    if (value.isBlank()) return null

    val patterns = listOf(
        DateTimeFormatter.ofPattern("M/d/yyyy"),
        DateTimeFormatter.ofPattern("MM/dd/yyyy"),
        DateTimeFormatter.ofPattern("M/d/yy"),
        DateTimeFormatter.ofPattern("MM/dd/yy")
    )

    for (formatter in patterns) {
        try {
            return LocalDate.parse(value, formatter)
        } catch (_: Exception) {
        }
    }

    return null
}
fun buildLookupSummaries(rows: List<LookupRow>): List<LookupSummary> {
    return rows
        .filter { !it.passengerId.isNullOrBlank() }
        .groupBy { it.passengerId!!.trim() }
        .map { (passengerId, passengerRows) ->
            val passenger =
                passengerRows
                    .firstNotNullOfOrNull { row ->
                        row.passenger
                            ?.let(::normalizePassengerNameForLookup)
                            ?.takeIf { it.isNotBlank() }
                    }
                    .orEmpty()
            val addresses =
                passengerRows
                    .flatMap { row ->
                        listOfNotNull(
                            row.pAddress,
                            row.dAddress
                        )
                    }
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                    .distinct()

            LookupSummary(
                passengerId = passengerId,
                passenger = passenger,
                tripCount = passengerRows.size,
                addresses = addresses
            )
        }
        .sortedBy { it.passenger }
}


private fun lookupDisplayName(raw: String): String {
    return raw
        .takeWhile { it != '+' && it != '(' }
        .trim()
}
fun buildLookupPassengerDetail(
    rows: List<LookupRow>,
    passengerId: String
): LookupPassengerDetail?
{
    val matches = rows
        .filter { row ->
            row.passengerId?.trim() == passengerId.trim()
        }

    if (matches.isEmpty()) return null

    val phone = matches
        .sortedByDescending { row ->
            parseLookupDriveDate(row.driveDate) ?: LocalDate.MIN
        }
        .firstOrNull { !it.phone.isNullOrBlank() }
        ?.phone
        ?.trim()

    val dayGroups = matches
        .groupBy { it.driveDate }
        .toList()
        .sortedByDescending { (driveDate, _) ->
            parseLookupDriveDate(driveDate) ?: LocalDate.MIN
        }
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
    val passengerName =
        matches
            .firstNotNullOfOrNull { row ->
                row.passenger
                    ?.let(::normalizePassengerNameForLookup)
                    ?.takeIf { it.isNotBlank() }
            }
            .orEmpty()

    return LookupPassengerDetail(
        passenger = passengerName,
        phone = phone,
        dayGroups = dayGroups
    )
}