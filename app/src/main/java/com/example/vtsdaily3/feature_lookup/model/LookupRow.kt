package com.example.vtsdaily3.feature_lookup.model

data class LookupRow(
    val date: String?,        // e.g. "2026-03-18"
    val time: String?,        // e.g. "08:30 PA"
    val passenger: String?,   // raw passenger name
    val phone: String?,       // phone number if present
    val pAddress: String?,    // pickup address
    val dAddress: String?,    // dropoff address
    val tripType: String?     // optional: "PA" / "PR" if already parsed
)