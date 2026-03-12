package com.example.vtsdaily3.feature_schedule.data

import android.content.Context
import com.example.vtsdaily3.data.ScheduleFolderPrefs

class PrefsScheduleFolderProvider(
    private val context: Context
) : ScheduleFolderProvider {

    override fun getScheduleFolderUriString(): String? {
        return ScheduleFolderPrefs.load(context)?.toString()
    }
}