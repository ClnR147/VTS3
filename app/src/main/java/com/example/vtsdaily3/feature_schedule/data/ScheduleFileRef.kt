package com.example.vtsdaily3.feature_schedule.data

import java.time.LocalDate

data class ScheduleFileRef(
    val date: LocalDate,
    val uriString: String,
    val displayName: String
)