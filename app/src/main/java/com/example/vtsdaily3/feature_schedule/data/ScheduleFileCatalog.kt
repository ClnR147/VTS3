package com.example.vtsdaily3.feature_schedule.data

import java.time.LocalDate

interface ScheduleFileCatalog {
    suspend fun getAvailableScheduleFiles(): List<ScheduleFileRef>
    suspend fun findScheduleFile(date: LocalDate): ScheduleFileRef?
}