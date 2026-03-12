package com.example.vtsdaily3.data

import android.content.Context
import android.net.Uri
import androidx.core.content.edit
import androidx.core.net.toUri

object ScheduleFolderPrefs {

    private const val PREFS = "schedule_prefs"
    private const val KEY_URI = "schedule_tree_uri"

    fun load(context: Context): Uri? {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val uriString = prefs.getString(KEY_URI, null)
        return uriString?.toUri()
    }

    fun save(context: Context, uri: Uri) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit { putString(KEY_URI, uri.toString()) }
    }
}