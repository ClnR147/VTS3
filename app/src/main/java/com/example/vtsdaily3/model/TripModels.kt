package com.example.vtsdaily3.model

import java.time.LocalDate

@JvmInline
value class TripId(val value: String) {
    companion object {
        fun stable(date: LocalDate, name: String, time: String, address: String): TripId =
            TripId("${date}|${time}|${name.trim()}|${address.trim()}".lowercase())
    }
}

enum class TripStatus { ACTIVE, COMPLETED, REMOVED, CANCELLED, NOSHOW }

enum class TripViewMode { ACTIVE, COMPLETED, OTHER }

data class Trip(
    val id: TripId,
    val date: LocalDate,
    val time: String,
    val name: String,
    val phone: String = "",
    val fromAddress: String = "",
    val toAddress: String = "",
    val status: TripStatus = TripStatus.ACTIVE
)

fun TripViewMode.toStatus(): TripStatus = when (this) {
    TripViewMode.ACTIVE -> TripStatus.ACTIVE
    TripViewMode.COMPLETED -> TripStatus.COMPLETED
    TripViewMode.OTHER -> TripStatus.REMOVED
}