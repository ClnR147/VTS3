package com.example.vtsdaily3.app.setup

import android.content.Context
import android.net.Uri

object FolderPrefs {
    private const val PREFS_NAME = "vts3_prefs"
    private const val KEY_SCHEDULE_FOLDER_URI = "schedule_folder_uri"

    fun saveScheduleFolderUri(context: Context, uri: Uri) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_SCHEDULE_FOLDER_URI, uri.toString())
            .apply()
    }

    fun getScheduleFolderUri(context: Context): Uri? {
        val value = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_SCHEDULE_FOLDER_URI, null)
            ?: return null

        return runCatching { Uri.parse(value) }.getOrNull()
    }

    fun clearScheduleFolderUri(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_SCHEDULE_FOLDER_URI)
            .apply()
    }
}