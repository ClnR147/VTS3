package com.example.vtsdaily3.feature_schedule.domain

data class OptimizedRoute(
    val stops: List<RouteStop>,
    val arrivalMinutes: List<Int>,
    val totalDrivingSeconds: Int,
    val outsideWindowMinutes: Int
)

object RouteOptimizer {

    fun optimize(
        stops: List<RouteStop>,
        travelSeconds: Array<IntArray>,
        startMinutes: Int
    ): OptimizedRoute? {

        if (stops.isEmpty()) return null

        val pickupIndexByTrip = mutableMapOf<Any, Int>()
        val dropoffIndexByTrip = mutableMapOf<Any, Int>()

        stops.forEachIndexed { index, stop ->
            when (stop.stopType) {
                RouteStopType.PICKUP ->
                    pickupIndexByTrip[stop.tripId] = index

                RouteStopType.DROPOFF ->
                    dropoffIndexByTrip[stop.tripId] = index
            }
        }

        var bestOrder: List<Int>? = null
        var bestArrivals: List<Int> = emptyList()
        var bestPenalty = Int.MAX_VALUE
        var bestDrivingSeconds = Int.MAX_VALUE

        fun search(
            order: List<Int>,
            used: Set<Int>,
            currentMinutes: Int,
            drivingSeconds: Int,
            penaltyMinutes: Int,
            arrivals: List<Int>
        ) {
            if (order.size == stops.size) {
                if (
                    penaltyMinutes < bestPenalty ||
                    (
                            penaltyMinutes == bestPenalty &&
                                    drivingSeconds < bestDrivingSeconds
                            )
                ) {
                    bestPenalty = penaltyMinutes
                    bestDrivingSeconds = drivingSeconds
                    bestOrder = order
                    bestArrivals = arrivals
                }

                return
            }

            for (nextIndex in stops.indices) {

                if (nextIndex in used) continue

                val stop = stops[nextIndex]

                if (stop.stopType == RouteStopType.DROPOFF) {
                    val pickupIndex =
                        pickupIndexByTrip[stop.tripId] ?: continue

                    if (pickupIndex !in used) continue
                }

                val driveSeconds =
                    if (order.isEmpty()) {
                        0
                    } else {
                        travelSeconds[order.last()][nextIndex]
                    }

                val arrivalMinutes =
                    currentMinutes + (driveSeconds / 60.0).toInt()

                var newPenalty = penaltyMinutes

                if (
                    stop.stopType == RouteStopType.PICKUP &&
                    stop.typeTime.startsWith("PR", ignoreCase = true)
                ) {
                    val scheduled =
                        parseFirstTime(stop.typeTime)

                    if (scheduled != null) {
                        val earliest = scheduled - 15
                        val latest = scheduled + 5

                        newPenalty += when {
                            arrivalMinutes < earliest ->
                                earliest - arrivalMinutes

                            arrivalMinutes > latest ->
                                arrivalMinutes - latest

                            else -> 0
                        }
                    }
                }

                search(
                    order = order + nextIndex,
                    used = used + nextIndex,
                    currentMinutes = arrivalMinutes,
                    drivingSeconds = drivingSeconds + driveSeconds,
                    penaltyMinutes = newPenalty,
                    arrivals = arrivals + arrivalMinutes
                )
            }
        }

        search(
            order = emptyList(),
            used = emptySet(),
            currentMinutes = startMinutes,
            drivingSeconds = 0,
            penaltyMinutes = 0,
            arrivals = emptyList()
        )

        val order = bestOrder ?: return null

        return OptimizedRoute(
            stops = order.map { stops[it] },
            arrivalMinutes = bestArrivals,
            totalDrivingSeconds = bestDrivingSeconds,
            outsideWindowMinutes = bestPenalty
        )
    }

    private fun parseFirstTime(typeTime: String): Int? {
        val match =
            Regex("""(\d{1,2}):(\d{2})""")
                .find(typeTime)
                ?: return null

        val hour = match.groupValues[1].toInt()
        val minute = match.groupValues[2].toInt()

        return hour * 60 + minute
    }
}