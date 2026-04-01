package com.example.vtsdaily3.feature_clinics.data

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.opencsv.CSVWriter
import java.io.File
import java.io.OutputStreamWriter

object ClinicStore {
    private const val FILE_NAME = "clinics.json"
    private val gson = Gson()

    fun load(context: Context): List<ClinicEntry> {
        val file = File(context.filesDir, FILE_NAME)
        if (!file.exists()) return emptyList()

        return try {
            val type = object : TypeToken<List<ClinicEntry>>() {}.type
            gson.fromJson<List<ClinicEntry>>(file.readText(), type) ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun save(context: Context, clinics: List<ClinicEntry>) {
        val file = File(context.filesDir, FILE_NAME)

        try {
            val sorted = clinics.sortedWith(
                compareBy({ it.name.lowercase() }, { it.address.lowercase() })
            )
            file.writeText(gson.toJson(sorted))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}



