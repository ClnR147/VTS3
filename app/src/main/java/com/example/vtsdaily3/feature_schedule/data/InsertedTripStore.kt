package com.example.vtsdaily3.feature_schedule.data


import android.content.Context
import com.example.vtsdaily3.model.Trip
import com.example.vtsdaily3.model.TripId
import com.example.vtsdaily3.model.TripStatus
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File
import java.time.LocalDate

object InsertedTripStore {

    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    private data class InsertedTripDto(
        val id: String,
        val date: String,
        val time: String,
        val name: String,
        val phone: String,
        val fromAddress: String,
        val toAddress: String,
        val status: String
    )

    private fun rootDir(context: Context): File {
        val dir = File(context.filesDir, "inserted_trips")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    private fun fileForDate(context: Context, date: LocalDate): File {
        return File(rootDir(context), "inserted_$date.json")
    }

    fun load(context: Context, date: LocalDate): List<Trip> {
        val file = fileForDate(context, date)
        if (!file.exists()) return emptyList()

        return runCatching {
            loadDtos(file).map { dto ->
                Trip(
                    id = TripId(dto.id),
                    date = LocalDate.parse(dto.date),
                    time = dto.time,
                    name = dto.name,
                    phone = dto.phone,
                    fromAddress = dto.fromAddress,
                    toAddress = dto.toAddress,
                    status = TripStatus.valueOf(dto.status)
                )
            }
        }.getOrElse {
            emptyList()
        }
    }

    fun save(context: Context, date: LocalDate, trips: List<Trip>) {
        val file = fileForDate(context, date)
        val dtos = trips.map { trip ->
            InsertedTripDto(
                id = trip.id.value,
                date = trip.date.toString(),
                time = trip.time,
                name = trip.name,
                phone = trip.phone,
                fromAddress = trip.fromAddress,
                toAddress = trip.toAddress,
                status = trip.status.name
            )
        }
        saveDtos(file, dtos)
    }

    fun add(context: Context, trip: Trip) {
        val file = fileForDate(context, trip.date)
        val existingDtos = loadDtos(file)

        val newDto = InsertedTripDto(
            id = trip.id.value,
            date = trip.date.toString(),
            time = trip.time,
            name = trip.name,
            phone = trip.phone,
            fromAddress = trip.fromAddress,
            toAddress = trip.toAddress,
            status = trip.status.name
        )

        saveDtos(file, existingDtos + newDto)
    }

    private fun loadDtos(file: File): List<InsertedTripDto> {
        if (!file.exists()) return emptyList()

        return runCatching {
            val json = file.readText()
            val type = object : TypeToken<List<InsertedTripDto>>() {}.type
            gson.fromJson<List<InsertedTripDto>>(json, type).orEmpty()
        }.getOrElse {
            emptyList()
        }
    }

    private fun saveDtos(file: File, dtos: List<InsertedTripDto>) {
        runCatching {
            file.parentFile?.mkdirs()
            file.writeText(gson.toJson(dtos))
        }
    }
}