package com.example.vtsdaily3.feature_schedule.data

import androidx.documentfile.provider.DocumentFile
import java.time.LocalDate

data class ScheduleFileEntry(
    val date: LocalDate,
    val file: DocumentFile
)