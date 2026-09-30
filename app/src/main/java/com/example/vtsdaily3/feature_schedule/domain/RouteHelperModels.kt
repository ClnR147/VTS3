package com.example.vtsdaily3.feature_schedule.domain

import com.example.vtsdaily3.model.Trip
import com.example.vtsdaily3.model.TripId

enum class RouteStopType {
    PICKUP,
    DROPOFF
}

data class RouteStop(
    val tripId: TripId,
    val passengerName: String,
    val typeTime: String,
    val stopType: RouteStopType,
    val address: String
)

fun Trip.toRouteStops(): List<RouteStop> {
    return listOf(
        RouteStop(
            tripId = id,
            passengerName = name,
            typeTime = time,
            stopType = RouteStopType.PICKUP,
            address = fromAddress
        ),
        RouteStop(
            tripId = id,
            passengerName = name,
            typeTime = time,
            stopType = RouteStopType.DROPOFF,
            address = toAddress
        )
    )
}
