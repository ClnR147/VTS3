package com.example.vtsdaily3.feature_lookup.domain

import com.example.vtsdaily3.feature_lookup.data.LookupRow
import com.example.vtsdaily3.feature_lookup.util.normalizePassengerNameForLookup
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
        .mapNotNull { row ->
            row.passenger
                ?.let(::normalizePassengerNameForLookup)
                ?.takeIf { it.isNotBlank() }
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


private fun lookupDisplayName(raw: String): String {
    return raw
        .takeWhile { it != '+' && it != '(' }
        .trim()
}

private fun parseDate(date: String?): Long {
    return try {
        val formatter = java.text.SimpleDateFormat("MM/dd/yyyy", java.util.Locale.US)
        formatter.parse(date ?: "")?.time ?: Long.MIN_VALUE
    } catch (e: Exception) {
        Long.MIN_VALUE
    }
}

private fun parseTime(row: LookupRow): Int {
    val time = when (row.tripType) {
        "appt" -> row.puTimeAppt
        "return" -> row.rtTime
        else -> row.puTimeAppt ?: row.rtTime
    } ?: return -1

    val match = Regex("""(\d{1,2}):(\d{2})""").find(time) ?: return -1
    val hour = match.groupValues[1].toIntOrNull() ?: return -1
    val minute = match.groupValues[2].toIntOrNull() ?: return -1

    return hour * 60 + minute
}
fun buildLookupPassengerDetail(
    rows: List<LookupRow>,
    passengerName: String
): LookupPassengerDetail? {
    val targetName = lookupDisplayName(passengerName)

    val matches = rows
        .filter { row ->
            normalizePassengerNameForLookup(row.passenger.orEmpty()) == targetName
        }

    if (matches.isEmpty()) return null

    val phone = matches
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

    return LookupPassengerDetail(
        passenger = targetName,
        phone = phone,
        dayGroups = dayGroups
    )
}

