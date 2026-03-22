package com.example.vtsdaily3.feature_contacts.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

object ContactStore {
    private const val FILE_NAME = "contacts.json"
    private val gson = Gson()

    fun load(context: Context): List<ContactEntry> {
        val file = File(context.filesDir, FILE_NAME)
        if (!file.exists()) return emptyList()

        return try {
            val type = object : TypeToken<List<ContactEntry>>() {}.type
            gson.fromJson<List<ContactEntry>>(file.readText(), type) ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun save(context: Context, contacts: List<ContactEntry>) {
        val file = File(context.filesDir, FILE_NAME)

        try {
            file.writeText(gson.toJson(contacts))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}