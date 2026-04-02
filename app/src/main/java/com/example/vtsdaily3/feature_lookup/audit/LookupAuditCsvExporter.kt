package com.example.vtsdaily3.feature_lookup.audit

import com.example.vtsdaily3.feature_lookup.model.DuplicateAddressAuditReport

object LookupAuditCsvExporter {

    fun toCsv(report: DuplicateAddressAuditReport): String {
        val header = listOf(
            "rowIndex",
            "date",
            "time",
            "passenger",
            "tripType",
            "rawPickupAddress",
            "rawDropoffAddress",
            "normalizedPickupAddress",
            "normalizedDropoffAddress",
            "reason"
        ).joinToString(",")

        val rows = report.findings.map { finding ->
            listOf(
                finding.rowIndex.toString(),
                escape(finding.date.orEmpty()),
                escape(finding.time.orEmpty()),
                escape(finding.passenger),
                escape(finding.tripType.orEmpty()),
                escape(finding.rawPickupAddress),
                escape(finding.rawDropoffAddress),
                escape(finding.normalizedPickupAddress),
                escape(finding.normalizedDropoffAddress),
                escape(finding.reason)
            ).joinToString(",")
        }

        return buildString {
            appendLine(header)
            rows.forEach { appendLine(it) }
        }
    }

    private fun escape(value: String): String {
        val escaped = value.replace("\"", "\"\"")
        return "\"$escaped\""
    }
}