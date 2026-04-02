package com.example.vtsdaily3.feature_lookup.audit

import android.content.Context
import com.example.vtsdaily3.feature_lookup.data.LookupStore
import com.example.vtsdaily3.feature_lookup.model.DuplicateAddressAuditFinding
import com.example.vtsdaily3.feature_lookup.model.DuplicateAddressAuditReport
import com.example.vtsdaily3.feature_lookup.data.LookupRow

object LookupAddressAudit {

    fun run(rows: List<LookupRow>): DuplicateAddressAuditReport {
        val findings = buildList {
            rows.forEachIndexed { index, row ->
                val rawPickup = row.pAddress.orEmpty().trim()
                val rawDropoff = row.dAddress.orEmpty().trim()

                if (rawPickup.isBlank() || rawDropoff.isBlank()) {
                    return@forEachIndexed
                }

                val normalizedPickup = LookupAddressNormalizer.normalize(rawPickup)
                val normalizedDropoff = LookupAddressNormalizer.normalize(rawDropoff)

                if (normalizedPickup.isBlank() || normalizedDropoff.isBlank()) {
                    return@forEachIndexed
                }

                if (normalizedPickup == normalizedDropoff) {
                    add(
                        DuplicateAddressAuditFinding(
                            rowIndex = index,
                            passenger = row.passenger.orEmpty().trim(),
                            tripType = row.tripType?.trim(),
                            rawPickupAddress = rawPickup,
                            rawDropoffAddress = rawDropoff,
                            normalizedPickupAddress = normalizedPickup,
                            normalizedDropoffAddress = normalizedDropoff,
                            date = row.driveDate?.trim(),
                            time = row.rtTime?.trim(),
                            reason = "pickup and dropoff normalize to the same value"
                        )
                    )
                }
            }
        }

        fun runFromStore(context: Context): DuplicateAddressAuditReport {
            val rows = LookupStore.load(context)
            return run(rows)
        }

        return DuplicateAddressAuditReport(
            totalRowsScanned = rows.size,
            totalFindings = findings.size,
            findings = findings
        )
    }
}
