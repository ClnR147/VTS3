package com.example.vtsdaily3.feature_clinics.export

data class ClinicAddressCandidate(
    val tripType: ClinicTripType,
    val address: String,
    val driveDate: String
)

enum class ClinicTripType {
    PA, PR
}