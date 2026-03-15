package com.example.vtsdaily3.feature_schedule.notes

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File

object PassengerNotesStore {

    private const val FILE_NAME = "PassengerResidenceNotes.json"
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    private fun file(context: Context): File {
        return File(context.filesDir, FILE_NAME)
    }

    private fun loadAll(context: Context): MutableMap<String, PassengerResidenceNote> {
        val file = file(context)
        if (!file.exists()) return mutableMapOf()

        return try {
            val json = file.readText()
            if (json.isBlank()) return mutableMapOf()

            val type = object : TypeToken<MutableMap<String, PassengerResidenceNote>>() {}.type
            gson.fromJson<MutableMap<String, PassengerResidenceNote>>(json, type)
                ?: mutableMapOf()
        } catch (_: Exception) {
            mutableMapOf()
        }
    }

    private fun saveAll(
        context: Context,
        records: Map<String, PassengerResidenceNote>
    ) {
        try {
            file(context).writeText(gson.toJson(records))
        } catch (_: Exception) {
        }
    }

    fun get(
        context: Context,
        recordKey: String
    ): PassengerResidenceNote? {
        return loadAll(context)[recordKey]
    }

    fun put(
        context: Context,
        note: PassengerResidenceNote
    ) {
        val all = loadAll(context)
        all[note.recordKey] = note
        saveAll(context, all)
    }
}
