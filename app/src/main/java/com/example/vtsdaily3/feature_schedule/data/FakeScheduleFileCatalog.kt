package com.example.vtsdaily3.feature_schedule.data

import java.time.LocalDate

class FakeScheduleFileCatalog : ScheduleFileCatalog {

    private val files = listOf(
        ScheduleFileRef(
            date = LocalDate.now().minusDays(1),
            uriString = "fake://schedule/yesterday.xls",
            displayName = "VTS fake-yesterday.xls"
        ),
        ScheduleFileRef(
            date = LocalDate.now(),
            uriString = "fake://schedule/today.xls",
            displayName = "VTS fake-today.xls"
        ),
        ScheduleFileRef(
            date = LocalDate.now().plusDays(1),
            uriString = "fake://schedule/tomorrow.xls",
            displayName = "VTS fake-tomorrow.xls"
        )
    )

    override suspend fun getAvailableScheduleFiles(): List<ScheduleFileRef> = files

    override suspend fun findScheduleFile(date: LocalDate): ScheduleFileRef? {
        return files.firstOrNull { it.date == date }
    }
}