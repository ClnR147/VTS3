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

    private var cachedFiles: List<ScheduleFileRef>? = null

    override suspend fun getAvailableScheduleFiles(): List<ScheduleFileRef> {
        cachedFiles?.let { cached ->

            return cached
        }

        val folderUriString = folderProvider.getScheduleFolderUriString()


        if (folderUriString.isNullOrBlank()) return emptyList()

        val folderUri = Uri.parse(folderUriString)
        val folder = DocumentFile.fromTreeUri(context, folderUri)

        if (folder == null) {

            return emptyList()
        }

        if (!folder.exists()) {

            return emptyList()
        }

        if (!folder.isDirectory) {

            return emptyList()
        }

        val results = folder.listFiles()
            .asSequence()
            .filter { it.isFile }
            .mapNotNull { file ->
                val name = file.name


                if (name.isNullOrBlank()) return@mapNotNull null

                val date = ScheduleFilenameParser.parseDateOrNull(name)


                if (date == null) return@mapNotNull null

                ScheduleFileRef(
                    date = date,
                    uriString = file.uri.toString(),
                    displayName = name
                )
            }
            .sortedBy { it.date }
            .toList()



        cachedFiles = results
        return results
    }

    override suspend fun findScheduleFile(date: LocalDate): ScheduleFileRef? {
        return getAvailableScheduleFiles().firstOrNull { it.date == date }
    }

    override suspend fun refresh() {

        cachedFiles = null
    }
}