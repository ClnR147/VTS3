package com.example.vtsdaily3.feature_clinics.domain

import com.example.vtsdaily3.feature_clinics.data.ClinicEntry

data class ClinicMatchResult(
    val clinic: ClinicEntry,
    val matchedAddress: String
)

private val weakAddressTokens = setOf(
    "st", "street",
    "rd", "road",
    "ave", "avenue",
    "dr", "drive",
    "blvd", "boulevard",
    "ln", "lane",
    "ct", "court",
    "pl", "place",
    "way",
    "hwy", "highway",
    "apt", "unit", "ste", "suite"
)

private fun cleanAddressForClinicMatch(raw: String?): String {
    return raw
        .orEmpty()
        .lowercase()
        .replace(",", " ")
        .replace(".", " ")
        .replace("#", " ")
        .replace(Regex("\\s+"), " ")
        .trim()
}

private fun extractStreetNumber(raw: String?): String? {
    return cleanAddressForClinicMatch(raw)
        .split(" ")
        .firstOrNull { token -> token.all(Char::isDigit) }
}

private fun extractStrongAddressTokens(raw: String?): Set<String> {
    return cleanAddressForClinicMatch(raw)
        .split(" ")
        .asSequence()
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .filterNot { token -> token.all(Char::isDigit) }          // ignore house number here
        .filterNot { token -> token.length == 5 && token.all(Char::isDigit) } // ignore zip
        .filterNot { token -> token in weakAddressTokens }
        .toSet()
}

private fun addressesMatchEnough(
    tripAddress: String?,
    clinicAddress: String?
): Boolean {
    val tripNumber = extractStreetNumber(tripAddress)
    val clinicNumber = extractStreetNumber(clinicAddress)

    val tripTokens = extractStrongAddressTokens(tripAddress)
    val clinicTokens = extractStrongAddressTokens(clinicAddress)

    val overlapCount = tripTokens.intersect(clinicTokens).size

    return if (tripNumber != null && clinicNumber != null) {
        tripNumber == clinicNumber && overlapCount >= 1
    } else {
        overlapCount >= 2
    }
}

fun findMatchingClinic(
    address: String?,
    clinics: List<ClinicEntry>
): ClinicEntry? {
    val candidate = address?.trim().orEmpty()
    if (candidate.isBlank()) return null

    return clinics.firstOrNull { clinic ->
        addressesMatchEnough(candidate, clinic.address)
    }
}

fun resolveClinicCandidateAddress(
    timeText: String,
    fromAddress: String?,
    toAddress: String?
): String {
    return if (timeText.contains("PA", ignoreCase = true)) {
        toAddress.orEmpty()
    } else {
        fromAddress.orEmpty()
    }
}

fun resolveClinicPhoneForTrip(
    timeText: String,
    fromAddress: String?,
    toAddress: String?,
    clinics: List<ClinicEntry>
): String? {
    val clinicAddress = resolveClinicCandidateAddress(
        timeText = timeText,
        fromAddress = fromAddress,
        toAddress = toAddress
    )

    val clinic = findMatchingClinic(
        address = clinicAddress,
        clinics = clinics
    )

    return clinic?.phone?.trim()?.takeIf { it.isNotBlank() }
}