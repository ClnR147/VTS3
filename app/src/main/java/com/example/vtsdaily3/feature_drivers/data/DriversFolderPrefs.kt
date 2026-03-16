package com.example.vtsdaily3.feature_drivers.data

import android.content.Context
import androidx.core.content.edit

object DriversFolderPrefs {
    private const val PREFS_NAME = "drivers_prefs"
    private const val KEY_FOLDER_URI = "drivers_folder_uri"

    fun saveFolderUri(context: Context, uri: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit {
                putString(KEY_FOLDER_URI, uri)
            }
    }

    fun getFolderUri(context: Context): String? {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_FOLDER_URI, null)
    }
}