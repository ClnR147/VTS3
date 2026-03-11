package com.example.vtsdaily3.feature_schedule.data

class PrefsScheduleFolderProvider(
    private val prefs: ScheduleFolderPrefs
) : ScheduleFolderProvider {

    override fun getScheduleFolderUriString(): String? {
        return prefs.getFolderUri()
    }
}