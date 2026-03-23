package com.example.vtsdaily3.feature_lookup.util

fun normalizePassengerNameForLookup(raw: String): String {
    val builder = StringBuilder()

    for (char in raw) {
        if (char.isLetter() || char.isWhitespace() || char == '-') {
            builder.append(char)
        } else {
            break
        }
    }

    return builder.toString()
        .replace(Regex("\\s+"), " ")
        .trim()
}