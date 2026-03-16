package com.example.vtsdaily3.feature_schedule.notes

private val parenSuffixRegex = Regex("\\s*\\([^)]*\\)\\s*$")
private val plusSuffixRegex = Regex("\\s*\\+\\d+\\s*$")
private val multiSpaceRegex = Regex("\\s+")

fun normalizePassengerNameForNotes(raw: String): String {
    return raw
        .trim()
        .replace(parenSuffixRegex, "")
        .replace(plusSuffixRegex, "")
        .replace(multiSpaceRegex, " ")
        .lowercase()
}

fun normalizeAddressForNotes(raw: String): String {
    return raw
        .trim()
        .replace(multiSpaceRegex, " ")
        .lowercase()
}

fun buildPassengerResidenceKey(
    passengerName: String,
    residenceAddress: String
): String {
    val passengerKey = normalizePassengerNameForNotes(passengerName)
    val addressKey = normalizeAddressForNotes(residenceAddress)
    return "$passengerKey||$addressKey"
}

fun displayPassengerNameForNotes(raw: String): String {
    return normalizePassengerNameForNotes(raw)
        .split(" ")
        .filter { it.isNotBlank() }
        .joinToString(" ") { part ->
            part.replaceFirstChar { ch ->
                if (ch.isLowerCase()) ch.titlecase() else ch.toString()
            }
        }
}
