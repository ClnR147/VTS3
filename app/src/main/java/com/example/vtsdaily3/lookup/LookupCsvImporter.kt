package com.example.vtsdaily3.lookup

import android.util.Log
import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderBuilder
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset

private const val TAG_IMPORT = "LookupCsvImport"

fun importLookupCsv(
    inputStream: InputStream,
    charset: Charset = Charsets.UTF_8
): List<LookupRow> {
    val bytes = inputStream.use { it.readBytes() }
    if (bytes.isEmpty()) return emptyList()

    val sep = detectCsvSeparator(bytes, charset)

    val parser = CSVParserBuilder()
        .withSeparator(sep)
        .withIgnoreQuotations(false)
        .build()

    CSVReaderBuilder(
        InputStreamReader(ByteArrayInputStream(bytes), charset)
    )
        .withCSVParser(parser)
        .build()
        .use { reader ->

            val headerRow = reader.readNext() ?: return emptyList()

            val clean = headerRow.mapIndexed { i, h ->
                val s = h.trim()
                if (i == 0) s.removePrefix("\uFEFF") else s
            }

            fun findIdx(vararg aliases: String): Int =
                indexOfHeader(clean, aliases.toList())

            data class Cols(
                val driveDate: Int,
                val passenger: Int,
                val ar: Int,
                val pAddr: Int,
                val dAddr: Int,
                val puAppt: Int,
                val doAppt: Int,
                val rt: Int,
                val phone: Int
            )

            val cols = Cols(
                driveDate = findIdx("DriveDate", "Date", "Drive Date"),
                passenger = findIdx("Passenger", "Name"),
                ar = findIdx("A/R", "AR", "TripType", "Type"),
                pAddr = findIdx("PAddress", "PickupAddress", "P Address"),
                dAddr = findIdx("DAddress", "DropAddress", "D Address"),
                puAppt = findIdx("PUTimeAppt", "puTimeAppt", "Appt", "ApptTime", "AppointmentTime"),
                doAppt = findIdx("DOTimeAppt", "doTimeAppt", "DropOffAppt", "Drop Appt"),
                rt = findIdx("RTTime", "ReturnTime", "RT"),
                phone = findIdx("Phone", "PhoneNumber")
            )

            val missingAny = listOf(
                "DriveDate" to cols.driveDate,
                "Passenger" to cols.passenger,
                "A/R" to cols.ar,
                "PAddress" to cols.pAddr,
                "DAddress" to cols.dAddr,
                "PUTimeAppt/puTimeAppt" to cols.puAppt,
                "DOTimeAppt/doTimeAppt" to cols.doAppt,
                "RTTime" to cols.rt,
                "Phone" to cols.phone
            ).filter { it.second < 0 }

            if (missingAny.isNotEmpty()) {
                Log.e(TAG_IMPORT, "Missing required/alias columns: $missingAny | Cleaned header=$clean")
                return emptyList()
            }

            dumpHeaderForLogcat(clean)

            fun at(row: Array<String>, i: Int): String? =
                row.getOrNull(i)?.trim()?.takeIf { it.isNotEmpty() }

            val out = mutableListOf<LookupRow>()

            reader.forEach { row ->
                val raw = mutableMapOf<String, String?>()

                clean.forEachIndexed { i, name ->
                    raw[name] = at(row, i)
                }

                raw["PUTimeAppt"] = at(row, cols.puAppt)
                raw["DOTimeAppt"] = at(row, cols.doAppt)
                raw["RTTime"] = at(row, cols.rt)

                val driveDate = at(row, cols.driveDate)
                val passenger = at(row, cols.passenger) ?: return@forEach
                val pAddr = at(row, cols.pAddr) ?: ""
                val dAddr = at(row, cols.dAddr) ?: ""
                val phone = at(row, cols.phone)

                val ar = at(row, cols.ar)
                val tripType = when (ar?.firstOrNull()?.uppercaseChar()) {
                    'A' -> "appt"
                    'R' -> "return"
                    else -> null
                }

                val pu = raw["PUTimeAppt"]
                val doAppt = raw["DOTimeAppt"]
                val rt = raw["RTTime"]

                out += LookupRow(
                    driveDate = driveDate,
                    passenger = passenger,
                    pAddress = pAddr,
                    dAddress = dAddr,
                    phone = phone,
                    tripType = tripType,
                    puTimeAppt = pu,
                    doTimeAppt = doAppt,
                    rtTime = rt,
                    raw = raw
                )
            }
            Log.d(TAG_IMPORT, "Imported rows: ${out.size}")

            out.take(5).forEach {
                Log.d(TAG_IMPORT, "Row: $it")
            }


            return out
        }
}

private fun indexOfHeader(header: List<String>, aliases: List<String>): Int {
    val normalizedHeader = header.map { normalizeHeader(it) }
    val normalizedAliases = aliases.map { normalizeHeader(it) }

    for (alias in normalizedAliases) {
        val idx = normalizedHeader.indexOf(alias)
        if (idx >= 0) return idx
    }
    return -1
}

private fun normalizeHeader(value: String): String {
    return value
        .trim()
        .removePrefix("\uFEFF")
        .lowercase()
        .replace("/", "")
        .replace("_", "")
        .replace(" ", "")
}

private fun detectCsvSeparator(
    bytes: ByteArray,
    charset: Charset
): Char {
    val firstLine = ByteArrayInputStream(bytes)
        .bufferedReader(charset)
        .use { it.readLine().orEmpty() }

    val commaCount = firstLine.count { it == ',' }
    val semicolonCount = firstLine.count { it == ';' }
    val tabCount = firstLine.count { it == '\t' }

    return when {
        tabCount > commaCount && tabCount > semicolonCount -> '\t'
        semicolonCount > commaCount -> ';'
        else -> ','
    }
}

private fun dumpHeaderForLogcat(header: List<String>) {
    Log.d(TAG_IMPORT, "CSV header: ${header.joinToString(" | ")}")
}
