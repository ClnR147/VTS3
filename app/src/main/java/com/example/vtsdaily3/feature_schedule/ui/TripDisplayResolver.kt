package com.example.vtsdaily3.feature_schedule.ui

import com.example.vtsdaily3.model.Trip

data class TripDisplayAddresses(
    val fromLabel: String,
    val fromValue: String,
    val toLabel: String,
    val toValue: String
)

fun resolveTripDisplayAddresses(trip: Trip): TripDisplayAddresses {
    return TripDisplayAddresses(
        fromLabel = "From:",
        fromValue = trip.fromAddress,
        toLabel = "To:",
        toValue = trip.toAddress
    )
}
