package com.example.vtsdaily3.feature_lookup.model

data class DuplicateAddressAuditFinding(
    val rowIndex: Int,
    val passenger: String,
    val tripType: String?,
    val rawPickupAddress: String,
    val rawDropoffAddress: String,
    val normalizedPickupAddress: String,
    val normalizedDropoffAddress: String,
    val date: String?,
    val time: String?,
    val reason: String
)

data class DuplicateAddressAuditReport(
    val totalRowsScanned: Int,
    val totalFindings: Int,
    val findings: List<DuplicateAddressAuditFinding>
)