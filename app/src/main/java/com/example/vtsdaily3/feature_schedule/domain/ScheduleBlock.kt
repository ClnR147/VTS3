package com.example.vtsdaily3.feature_schedule.domain

import java.time.LocalDate

data class ScheduleBlock(
    val date: LocalDate,
    val title: String,
    val startTime: String,
    val endTime: String,
    val notes: String = ""
)