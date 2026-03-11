package com.example.vtsdaily3.feature_schedule.data


import android.content.Context

class ScheduleFolderPrefs(context: Context) {

    private val prefs = context.getSharedPreferences("schedule_folder_prefs", Context.MODE_PRIVATE)

    fun saveFolderUri(uriString: String) {
        prefs.edit().putString(KEY_FOLDER_URI, uriString).apply()
    }

    fun getFolderUri(): String? {
        return prefs.getString(KEY_FOLDER_URI, null)
    }

    companion object {
        private const val KEY_FOLDER_URI = "schedule_folder_uri"
    }
}