package com.example.vtsdaily3.feature_drivers.data

object DriverImportParser {

    fun parseCsv(text: String): List<DriverContact> {
        val lines = text
            .lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }

        if (lines.isEmpty()) return emptyList()

        return lines
            .drop(1) // assumes first line is header
            .mapNotNull { parseLine(it) }
    }

    private fun parseLine(line: String): DriverContact? {
        val parts = line.split(",").map { it.trim() }

        val name = parts.getOrNull(0).orEmpty()
        val phone = parts.getOrNull(1).orEmpty()
        val vanNumber = parts.getOrNull(2).orEmpty()

        if (name.isBlank()) return null

        return DriverContact(
            name = name,
            phone = phone,
            vanNumber = vanNumber
        )
    }
}