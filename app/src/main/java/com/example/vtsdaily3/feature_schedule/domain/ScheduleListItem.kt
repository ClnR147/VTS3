package com.example.vtsdaily3.feature_schedule.domain

import com.example.vtsdaily3.model.Trip

sealed class ScheduleListItem {

    data class TripItem(
        val trip: Trip
    ) : ScheduleListItem()

    data class BlockItem(
        val block: ScheduleBlock
    ) : ScheduleListItem()
}