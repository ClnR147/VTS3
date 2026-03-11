package com.example.vtsdaily3.feature_schedule.data

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.core.net.toUri

class AndroidScheduleFileCatalog(
    private val context: Context,
    private val folderProvider: ScheduleFolderProvider
) : ScheduleFileCatalog {

    override suspend fun getAvailableScheduleFiles(): List<ScheduleFileRef> {
        val folderUriString = folderProvider.getScheduleFolderUriString() ?: return emptyList()
        val folderUri = folderUriString.toUri()

        val folder = DocumentFile.fromTreeUri(context, folderUri) ?: return emptyList()
        if (!folder.exists() || !folder.isDirectory) return emptyList()

        return folder.listFiles()
            .filter { it.isFile }
            .mapNotNull { file ->
                val name = file.name ?: return@mapNotNull null
                val date = ScheduleFilenameParser.parseDateOrNull(name) ?: return@mapNotNull null
                val uriString = file.uri.toString()

                ScheduleFileRef(
                    date = date,
                    uriString = uriString,
                    displayName = name
                )
            }
            .sortedBy { it.date }
    }

    override suspend fun findScheduleFile(date: java.time.LocalDate): ScheduleFileRef? {
        return getAvailableScheduleFiles().firstOrNull { it.date == date }
    }
}