package com.example.vtsdaily3.feature_schedule.data

import java.time.LocalDate

object ScheduleFilenameParser {

    private val dateRegex = Regex("""(\d{1,2})-(\d{1,2})-(\d{2}|\d{4})""")

    fun parseDateOrNull(fileName: String): LocalDate? {
        val lower = fileName.lowercase()

        if (!lower.endsWith(".xls")) return null

        val match = dateRegex.find(fileName) ?: return null

        val month = match.groupValues[1].toIntOrNull() ?: return null
        val day = match.groupValues[2].toIntOrNull() ?: return null
        val yearRaw = match.groupValues[3].toIntOrNull() ?: return null

        val year = if (yearRaw < 100) 2000 + yearRaw else yearRaw

        return try {
            LocalDate.of(year, month, day)
        } catch (_: Exception) {
            null
        }
    }
}