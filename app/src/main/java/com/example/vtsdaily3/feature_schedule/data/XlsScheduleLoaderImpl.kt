package com.example.vtsdaily3.feature_schedule.data

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.example.vtsdaily3.model.Trip
import com.example.vtsdaily3.model.TripId
import com.example.vtsdaily3.model.TripStatus
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.DateUtil
import org.apache.poi.ss.usermodel.Row
import java.io.InputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class XlsScheduleLoaderImpl(
    private val context: Context,
    private val folderUri: Uri
) : XlsScheduleLoader {

    private var dateIndex: Map<LocalDate, Uri> = emptyMap()

    override suspend fun getAvailableDates(): List<LocalDate> {
        ensureIndex()
        return dateIndex.keys.sorted()
    }

    override suspend fun loadTrips(date: LocalDate): List<Trip> {
        ensureIndex()

        val fileUri = dateIndex[date] ?: return emptyList()
        val input = context.contentResolver.openInputStream(fileUri) ?: return emptyList()

        input.use {
            return parseXlsInputStream(it, date)
        }
    }

    fun invalidateIndex() {
        dateIndex = emptyMap()
    }

    private fun ensureIndex() {
        if (dateIndex.isNotEmpty()) return

        val folder = DocumentFile.fromTreeUri(context, folderUri) ?: run {
            dateIndex = emptyMap()
            return
        }

        dateIndex = folder.listFiles()
            .filter { file ->
                file.isFile &&
                        file.name?.endsWith(".xls", ignoreCase = true) == true
            }
            .mapNotNull { file ->
                val name = file.name ?: return@mapNotNull null
                val date = extractDateFromFilename(name) ?: return@mapNotNull null
                date to file.uri
            }
            .toMap()
    }

    private fun extractDateFromFilename(name: String): LocalDate? {
        val regex = Regex("""(\d{2})-(\d{2})-(\d{2})""")
        val match = regex.find(name) ?: return null
        val formatter = DateTimeFormatter.ofPattern("MM-dd-yy")

        return runCatching {
            LocalDate.parse(match.value, formatter)
        }.getOrNull()
    }

    private fun parseXlsInputStream(
        input: InputStream,
        date: LocalDate
    ): List<Trip> {
        val trips = mutableListOf<Trip>()

        HSSFWorkbook(input).use { workbook ->
            val sheet = workbook.getSheetAt(0)

            val headerRowIndex = (0..minOf(sheet.lastRowNum, 30)).firstOrNull { rowIndex ->
                val row = sheet.getRow(rowIndex) ?: return@firstOrNull false
                row.physicalNumberOfCells > 0 &&
                        (0 until row.lastCellNum).any { col ->
                            cellString(row, col).trim().isNotEmpty()
                        }
            } ?: 0

            for (r in (headerRowIndex + 1)..sheet.lastRowNum) {
                val row = sheet.getRow(r) ?: continue

                val nameRaw = cellString(row, 0).trim()
                val fromAddr = cellString(row, 2).trim()
                val toAddr = cellString(row, 3).trim()
                val time = cellString(row, 4).trim()
                val phone = cellString(row, 5).trim()

                if (nameRaw.isBlank()) continue
                if (fromAddr.isBlank() && toAddr.isBlank()) continue

                val timeForId = normalizeTimeForId(time)

                trips += Trip(
                    id = TripId.stable(date, nameRaw, timeForId, fromAddr),
                    date = date,
                    time = time,
                    name = nameRaw,
                    phone = phone,
                    fromAddress = fromAddr,
                    toAddress = toAddr,
                    status = TripStatus.ACTIVE
                )
            }
        }

        Log.d("VTS3", "Parsed trips: ${trips.size}")

        return trips
    }

    private fun cellString(row: Row, cellIndex: Int): String {
        val cell = row.getCell(cellIndex) ?: return ""

        return when (cell.cellType) {
            CellType.STRING -> cell.stringCellValue.orEmpty().trim()

            CellType.NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    cell.localDateTimeCellValue?.toLocalTime()?.toString().orEmpty()
                } else {
                    val n = cell.numericCellValue
                    if (n % 1.0 == 0.0) n.toLong().toString() else n.toString()
                }
            }

            CellType.BOOLEAN -> cell.booleanCellValue.toString()

            CellType.FORMULA -> {
                when (cell.cachedFormulaResultType) {
                    CellType.STRING -> cell.stringCellValue.orEmpty().trim()
                    CellType.NUMERIC -> {
                        if (DateUtil.isCellDateFormatted(cell)) {
                            cell.localDateTimeCellValue?.toLocalTime()?.toString().orEmpty()
                        } else {
                            val n = cell.numericCellValue
                            if (n % 1.0 == 0.0) n.toLong().toString() else n.toString()
                        }
                    }
                    CellType.BOOLEAN -> cell.booleanCellValue.toString()
                    else -> ""
                }
            }

            else -> ""
        }
    }

    private fun normalizeTimeForId(raw: String): String {
        return raw.trim()
            .uppercase()
            .replace(".", "")
            .replace(Regex("\\s+"), " ")
    }
}