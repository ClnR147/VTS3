package com.example.vtsdaily3.feature_schedule.data

import android.content.Context
import com.example.vtsdaily3.model.TripId
import com.example.vtsdaily3.model.TripStatus
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.time.LocalDate

class TripStatusStoreImpl(
    private val context: Context
) : TripStatusStore {

    private val gson = Gson()

    private val statusDir: File
        get() {
            val dir = File(context.filesDir, "trip-status")
            if (!dir.exists()) dir.mkdirs()
            return dir
        }

    override suspend fun loadStatuses(date: LocalDate): List<TripStatusRecord> {
        val file = statusFile(date)

        if (!file.exists()) return emptyList()

        val json = file.readText()

        val type = object : TypeToken<List<TripStatusRecord>>() {}.type

        return runCatching {
            gson.fromJson<List<TripStatusRecord>>(json, type)
        }.getOrDefault(emptyList())
    }

    override suspend fun setStatus(
        date: LocalDate,
        tripId: TripId,
        status: TripStatus
    ) {
        val records = loadStatuses(date).toMutableList()

        val key = tripId.value

        val existingIndex = records.indexOfFirst { it.tripId == key }

        if (existingIndex >= 0) {
            records[existingIndex] = TripStatusRecord(key, status)
        } else {
            records.add(TripStatusRecord(key, status))
        }

        save(date, records)
    }

    override suspend fun clearStatus(
        date: LocalDate,
        tripId: TripId
    ) {
        val records = loadStatuses(date)
            .filter { it.tripId != tripId.value }

        save(date, records)
    }

    private fun save(
        date: LocalDate,
        records: List<TripStatusRecord>
    ) {
        val file = statusFile(date)

        val json = gson.toJson(records)

        file.writeText(json)
    }

    private fun statusFile(date: LocalDate): File {
        return File(statusDir, "$date.json")
    }
}