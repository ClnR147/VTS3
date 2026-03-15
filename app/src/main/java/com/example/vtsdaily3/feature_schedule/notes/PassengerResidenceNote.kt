package com.example.vtsdaily3.feature_schedule.notes
enum class ResidenceSide {
    PU, DO
}

data class PassengerResidenceNote(
    val recordKey: String,
    val passengerKey: String,
    val displayPassengerName: String,
    val residenceAddressKey: String,
    val displayResidenceAddress: String,
    val residenceSide: ResidenceSide,
    val gateCode: String = "",
    val noteText: String = ""
)
