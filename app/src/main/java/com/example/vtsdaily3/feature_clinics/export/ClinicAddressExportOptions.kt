package com.example.vtsdaily3.feature_clinics.export

data class ClinicAddressExportOptions(
    val dedupe: Boolean = false,
    val excludeKnownClinics: Boolean = true
)