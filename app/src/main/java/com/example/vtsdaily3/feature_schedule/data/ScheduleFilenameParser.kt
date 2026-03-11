package com.example.vtsdaily3.feature_schedule.data

import java.time.LocalDate

object ScheduleFilenameParser {

    private val dateRegex = Regex("""(\d{2})-(\d{2})-(\d{2})""")

    fun parseDateOrNull(fileName: String): LocalDate? {
        if (!fileName.lowercase().endsWith(".xls")) return null

        val match = dateRegex.find(fileName) ?: return null

        val month = match.groupValues[1].toIntOrNull() ?: return null
        val day = match.groupValues[2].toIntOrNull() ?: return null
        val year2 = match.groupValues[3].toIntOrNull() ?: return null

        val year = 2000 + year2

        return try {
            LocalDate.of(year, month, day)
        } catch (_: Exception) {
            null
        }
    }
}