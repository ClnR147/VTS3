package com.example.vtsdaily3.feature_clinics.export

import com.example.vtsdaily3.feature_lookup.data.LookupRow

class ClinicAddressCandidateExtractor {

    fun extract(
        rows: List<LookupRow>,
        knownClinicAddresses: Set<String> = emptySet(),
        options: ClinicAddressExportOptions = ClinicAddressExportOptions()
    ): List<ClinicAddressCandidate> {
        val results = mutableListOf<ClinicAddressCandidate>()
        val seenAddresses = mutableSetOf<String>()

        rows.forEach { row ->
            val candidate = extractRow(row) ?: return@forEach

            if (options.excludeKnownClinics) {
                val knownKey = normalizeAddress(candidate.address)
                if (knownKey in knownClinicAddresses) return@forEach
            }

            if (options.dedupe) {
                val dedupeKey = normalizeAddress(candidate.address)
                if (dedupeKey in seenAddresses) return@forEach
                seenAddresses.add(dedupeKey)
            }

            results.add(candidate)
        }

        android.util.Log.d("ClinicExport", "Final candidate count: ${results.size}")
        return results
    }

    private fun extractRow(row: LookupRow): ClinicAddressCandidate? {
        val tripType = resolveTripTypeFromApptReturn(row) ?: return null

        val selectedAddress = when (tripType) {
            ClinicTripType.PR -> row.pAddress
            ClinicTripType.PA -> row.dAddress
        }

        val cleanedAddress = cleanAddress(selectedAddress)
        if (!isUsableAddress(cleanedAddress)) return null

        val date = row.driveDate?.trim().orEmpty()

        return ClinicAddressCandidate(
            tripType = tripType,
            address = cleanedAddress,
            driveDate = date
        )
    }

    private fun resolveTripTypeFromApptReturn(row: LookupRow): ClinicTripType? {
        return when (row.tripType?.trim()?.lowercase()) {
            "appt" -> ClinicTripType.PA
            "return" -> ClinicTripType.PR
            else -> null
        }
    }

    private fun cleanAddress(address: String?): String {
        return address
            .orEmpty()
            .replace("\\s+".toRegex(), " ")
            .trim()
            .trim(',', ';')
    }

    private fun isUsableAddress(address: String): Boolean {
        if (address.isBlank()) return false

        val junkValues = setOf("N/A", "NA", "NONE", "UNKNOWN", "TBD", "?", "-", "--")
        if (address.uppercase() in junkValues) return false
        if (address.length < 6) return false

        return true
    }

    private fun normalizeAddress(address: String): String {
        return address
            .lowercase()
            .replace('\u00A0', ' ')
            .replace(".", "")
            .replace(",", "")
            .replace(";", "")
            .replace("\\s+".toRegex(), " ")
            .trim()
    }
}