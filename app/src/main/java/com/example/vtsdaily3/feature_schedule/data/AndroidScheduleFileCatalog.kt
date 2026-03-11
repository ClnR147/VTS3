package com.example.vtsdaily3.feature_schedule.data

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import java.time.LocalDate

class AndroidScheduleFileCatalog(
    private val context: Context,
    private val folderProvider: ScheduleFolderProvider
) : ScheduleFileCatalog {

    override suspend fun getAvailableScheduleFiles(): List<ScheduleFileRef> {
        val folderUriString = folderProvider.getScheduleFolderUriString()
        Log.d("ScheduleCatalog", "Saved folder URI = $folderUriString")

        if (folderUriString.isNullOrBlank()) return emptyList()

        val folderUri = Uri.parse(folderUriString)
        val folder = DocumentFile.fromTreeUri(context, folderUri)

        if (folder == null) {
            Log.d("ScheduleCatalog", "DocumentFile.fromTreeUri returned null")
            return emptyList()
        }

        if (!folder.exists()) {
            Log.d("ScheduleCatalog", "Folder does not exist")
            return emptyList()
        }

        if (!folder.isDirectory) {
            Log.d("ScheduleCatalog", "Selected URI is not a directory")
            return emptyList()
        }

        val results = folder.listFiles()
            .filter { it.isFile }
            .mapNotNull { file ->
                val name = file.name
                Log.d("ScheduleCatalog", "Found file = $name")

                if (name.isNullOrBlank()) return@mapNotNull null

                val date = ScheduleFilenameParser.parseDateOrNull(name)
                Log.d("ScheduleCatalog", "Parsed date for $name = $date")

                if (date == null) return@mapNotNull null

                ScheduleFileRef(
                    date = date,
                    uriString = file.uri.toString(),
                    displayName = name
                )
            }
            .sortedBy { it.date }

        Log.d("ScheduleCatalog", "Available schedule count = ${results.size}")

        return results
    }

    override suspend fun findScheduleFile(date: LocalDate): ScheduleFileRef? {
        return getAvailableScheduleFiles().firstOrNull { it.date == date }
    }
}