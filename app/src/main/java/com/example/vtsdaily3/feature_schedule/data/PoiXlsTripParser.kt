package com.example.vtsdaily3.feature_schedule.data

import android.content.Context
import com.example.vtsdaily3.model.Trip
import com.example.vtsdaily3.model.TripId
import com.example.vtsdaily3.model.TripStatus
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell
import java.io.InputStream
import java.time.LocalDate
import androidx.core.net.toUri

class PoiXlsTripParser(
    private val context: Context
) : XlsTripParser {

    override suspend fun parse(fileRef: ScheduleFileRef): List<Trip> {
        val uri = fileRef.uriString.toUri()

        val inputStream = context.contentResolver.openInputStream(uri) ?: return emptyList()

        return inputStream.use { input ->
            parseXlsInputStream(
                input = input,
                date = fileRef.date
            )
        }
    }

    private fun parseXlsInputStream(
        input: InputStream,
        date: LocalDate
    ): List<Trip> {
        val workbook = HSSFWorkbook(input)
        workbook.use { wb ->
            val sheet = wb.getSheetAt(0) ?: return emptyList()

            val headerRowIndex = (0..minOf(sheet.lastRowNum, 30)).firstOrNull { rowIndex ->
                val row = sheet.getRow(rowIndex) ?: return@firstOrNull false
                row.any { cell ->
                    cell != null && cellString(cell).isNotBlank()
                }
            } ?: 0

            val trips = mutableListOf<Trip>()

            for (r in headerRowIndex..sheet.lastRowNum) {
                val row = sheet.getRow(r) ?: continue

                val nameRaw = cellString(row.getCell(0)).trim()      // A = Name
                val fromAddr = cellString(row.getCell(2)).trim()     // C = From Address
                val toAddr = cellString(row.getCell(3)).trim()       // D = To Address
                val time = cellString(row.getCell(4)).trim()         // E = Time
                val phone = cellString(row.getCell(5)).trim()        // F = Phone

                if (nameRaw.isBlank() && fromAddr.isBlank() && toAddr.isBlank() && time.isBlank()) {
                    continue
                }

                if (nameRaw.isBlank()) continue

                val timeForId = if (time.isNotBlank()) time else "NO_TIME"
                val fromForId = if (fromAddr.isNotBlank()) fromAddr else "NO_FROM"

                trips += Trip(
                    id = TripId.stable(date, nameRaw, timeForId, fromForId),
                    date = date,
                    time = time,
                    name = nameRaw,
                    phone = phone,
                    fromAddress = fromAddr,
                    toAddress = toAddr,
                    status = TripStatus.ACTIVE
                )
            }

            return trips
        }
    }

    private fun cellString(cell: Cell?): String {
        if (cell == null) return ""

        return when (cell.cellType) {
            org.apache.poi.ss.usermodel.CellType.STRING -> cell.stringCellValue.orEmpty().trim()

            org.apache.poi.ss.usermodel.CellType.NUMERIC -> {
                val numeric = cell.numericCellValue
                if (numeric % 1.0 == 0.0) {
                    numeric.toLong().toString()
                } else {
                    numeric.toString()
                }
            }

            org.apache.poi.ss.usermodel.CellType.BOOLEAN -> cell.booleanCellValue.toString()

            org.apache.poi.ss.usermodel.CellType.FORMULA -> {
                when (cell.cachedFormulaResultType) {
                    org.apache.poi.ss.usermodel.CellType.STRING -> cell.stringCellValue.orEmpty().trim()

                    org.apache.poi.ss.usermodel.CellType.NUMERIC -> {
                        val numeric = cell.numericCellValue
                        if (numeric % 1.0 == 0.0) {
                            numeric.toLong().toString()
                        } else {
                            numeric.toString()
                        }
                    }

                    org.apache.poi.ss.usermodel.CellType.BOOLEAN -> cell.booleanCellValue.toString()
                    else -> ""
                }
            }

            else -> ""
        }
    }
}