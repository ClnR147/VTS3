package com.example.vtsdaily3.feature_drivers.data

import android.content.Context
import androidx.documentfile.provider.DocumentFile
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import androidx.core.net.toUri

object DriverStore {
    private const val FILE_NAME = "drivers.json"
    private val gson = Gson()

    fun load(context: Context): List<DriverContact> {
        val folderUriString = DriversFolderPrefs.getFolderUri(context) ?: return emptyList()
        val folderUri = folderUriString.toUri()
        val folder = DocumentFile.fromTreeUri(context, folderUri) ?: return emptyList()

        val file = folder.findFile(FILE_NAME) ?: return emptyList()

        return try {
            context.contentResolver.openInputStream(file.uri)?.use { input ->
                BufferedReader(InputStreamReader(input)).use { reader ->
                    val type = object : TypeToken<List<DriverContact>>() {}.type
                    gson.fromJson<List<DriverContact>>(reader, type) ?: emptyList()
                }
            } ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun save(context: Context, drivers: List<DriverContact>) {
        val folderUriString = DriversFolderPrefs.getFolderUri(context) ?: return
        val folderUri = folderUriString.toUri()
        val folder = DocumentFile.fromTreeUri(context, folderUri) ?: return

        val existing = folder.findFile(FILE_NAME)
        val file = existing ?: folder.createFile("application/json", FILE_NAME) ?: return

        try {
            context.contentResolver.openOutputStream(file.uri, "wt")?.use { output ->
                OutputStreamWriter(output).use { writer ->
                    gson.toJson(drivers, writer)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}