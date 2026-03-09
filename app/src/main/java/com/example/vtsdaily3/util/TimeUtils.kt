package com.example.vtsdaily3.util


private val TIME_REGEX = Regex("""(\d{1,2}):(\d{2})""")

fun normalizeTimeForId(raw: String): String {
    val m = TIME_REGEX.find(raw.trim()) ?: return "00:00"
    val h = m.groupValues[1].padStart(2, '0')
    val min = m.groupValues[2]
    return "$h:$min"
}

fun sortMinutes(raw: String): Int {
    val m = TIME_REGEX.find(raw.trim()) ?: return Int.MAX_VALUE
    val h = m.groupValues[1].toIntOrNull() ?: return Int.MAX_VALUE
    val min = m.groupValues[2].toIntOrNull() ?: return Int.MAX_VALUE
    return h * 60 + min
}
