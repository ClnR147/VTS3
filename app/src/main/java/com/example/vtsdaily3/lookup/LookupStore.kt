package com.example.vtsdaily3.lookup

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

object LookupStore {

    private const val FILE_NAME = "lookup_rows.json"
    private val gson = Gson()

    fun save(context: Context, rows: List<LookupRow>) {
        runCatching {
            val file = File(context.filesDir, FILE_NAME)
            file.writeText(gson.toJson(rows))
        }
    }

    fun load(context: Context): List<LookupRow> {
        return runCatching {
            val file = File(context.filesDir, FILE_NAME)
            if (!file.exists()) return emptyList()

            val json = file.readText()
            if (json.isBlank()) return emptyList()

            val type = object : TypeToken<List<LookupRow>>() {}.type
            gson.fromJson<List<LookupRow>>(json, type) ?: emptyList()
        }.getOrElse { emptyList() }
    }

    fun clear(context: Context) {
        runCatching {
            val file = File(context.filesDir, FILE_NAME)
            if (file.exists()) {
                file.delete()
            }
        }
    }
}