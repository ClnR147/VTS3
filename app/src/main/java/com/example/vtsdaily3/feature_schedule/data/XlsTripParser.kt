
package com.example.vtsdaily3.feature_schedule.data

import com.example.vtsdaily3.model.Trip

interface XlsTripParser {
    suspend fun parse(fileRef: ScheduleFileRef): List<Trip>
}