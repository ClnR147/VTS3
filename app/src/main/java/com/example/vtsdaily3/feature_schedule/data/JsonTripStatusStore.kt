package com.example.vtsdaily3.feature_schedule.data

import android.content.Context
import com.example.vtsdaily3.model.TripId
import com.example.vtsdaily3.model.TripStatus
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate

class JsonTripStatusStore(
    private val context: Context,
    private val gson: Gson = Gson()
) : TripStatusStore {

    override suspend fun loadStatuses(date: LocalDate): List<TripStatusRecord> =
        withContext(Dispatchers.IO) {
            val file = fileForDate(date)
            if (!file.exists()) return@withContext emptyList()

            val json = file.readText()
            if (json.isBlank()) return@withContext emptyList()

            val type = object : TypeToken<List<StoredStatus>>() {}.type

            val stored: List<StoredStatus> = runCatching {
                gson.fromJson<List<StoredStatus>>(json, type)
            }.getOrElse { emptyList() }

            stored.mapNotNull { item ->
                val status = runCatching { TripStatus.valueOf(item.status) }.getOrNull()
                    ?: return@mapNotNull null

                TripStatusRecord(
                    tripId = item.tripId,
                    status = status
                )
            }
        }

    override suspend fun setStatus(
        date: LocalDate,
        tripId: TripId,
        status: TripStatus
    ) = withContext(Dispatchers.IO) {
        val existing = loadStored(date).toMutableList()

        val updated = existing
            .filterNot { it.tripId == tripId.value }
            .toMutableList()
            .apply {
                add(
                    StoredStatus(
                        tripId = tripId.value,
                        status = status.name
                    )
                )
            }

        write(date, updated)
    }

    override suspend fun clearStatus(
        date: LocalDate,
        tripId: TripId
    ) = withContext(Dispatchers.IO) {
        val updated = loadStored(date)
            .filterNot { it.tripId == tripId.value }

        write(date, updated)
    }

    private fun loadStored(date: LocalDate): List<StoredStatus> {
        val file = fileForDate(date)
        if (!file.exists()) return emptyList()

        val json = file.readText()
        if (json.isBlank()) return emptyList()

        val type = object : TypeToken<List<StoredStatus>>() {}.type

        return runCatching {
            gson.fromJson<List<StoredStatus>>(json, type)
        }.getOrElse { emptyList() }
    }

    private fun write(date: LocalDate, records: List<StoredStatus>) {
        val file = fileForDate(date)
        file.parentFile?.mkdirs()
        file.writeText(gson.toJson(records))
    }

    private fun fileForDate(date: LocalDate): File {
        val dir = File(context.filesDir, "trip_status")
        return File(dir, "$date.json")
    }

    private data class StoredStatus(
        val tripId: String,
        val status: String
    )
}