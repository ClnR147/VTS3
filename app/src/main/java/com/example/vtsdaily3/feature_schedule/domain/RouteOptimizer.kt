package com.example.vtsdaily3.feature_schedule.domain

data class OptimizedRoute(
    val stops: List<RouteStop>,
    val arrivalMinutes: List<Int>,
    val totalDrivingSeconds: Int,
    val outsideWindowMinutes: Int,
    val passengerRideMinutes: Int
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

        val paGroupEarliestPickupByTrip = mutableMapOf<Any, Int>()
        val paGroupLatestPickupByTrip = mutableMapOf<Any, Int>()

        val paPickupStops =
            stops.filter {
                it.stopType == RouteStopType.PICKUP &&
                        it.typeTime.startsWith("PA", ignoreCase = true)
            }

        val paGroups =
            paPickupStops.groupBy { pickup ->

                val dropoffIndex =
                    dropoffIndexByTrip[pickup.tripId]

                val dropoff =
                    dropoffIndex?.let { stops[it] }

                val appointmentTime =
                    parseSecondTime(pickup.typeTime)

                if (dropoff != null && appointmentTime != null) {
                    "${dropoff.address.trim().lowercase()}|$appointmentTime"
                } else {
                    pickup.tripId.toString()
                }
            }

        paGroups.values
            .filter { it.size > 1 }
            .forEach { group ->

                val pickupTimes =
                    group.mapNotNull {
                        parseFirstTime(it.typeTime)
                    }

                if (pickupTimes.isNotEmpty()) {

                    val earliest = pickupTimes.min()
                    val latest = pickupTimes.max()

                    group.forEach { pickup ->
                        paGroupEarliestPickupByTrip[pickup.tripId] =
                            earliest

                        paGroupLatestPickupByTrip[pickup.tripId] =
                            latest
                    }
                }
            }

        val paGroupMembersByTrip = mutableMapOf<Any, Set<Any>>()

        paGroups.values
            .filter { it.size > 1 }
            .forEach { group ->

                val tripIds = group.map { it.tripId }.toSet()

                group.forEach { pickup ->
                    paGroupMembersByTrip[pickup.tripId] = tripIds
                }
            }

        var bestOrder: List<Int>? = null
        var bestArrivals: List<Int> = emptyList()
        var bestPenalty = Int.MAX_VALUE
        var bestDrivingSeconds = Int.MAX_VALUE
        var bestPassengerRideMinutes = Int.MAX_VALUE
        var bestCombinedCost = Int.MAX_VALUE

        fun search(
            order: List<Int>,
            used: Set<Int>,
            currentMinutes: Int,
            drivingSeconds: Int,
            penaltyMinutes: Int,
            passengerRideMinutes: Int,
            pickupTimes: Map<Any, Int>,
            arrivals: List<Int>
        ) {
            if (order.size == stops.size) {
                val drivingMinutes =
                    (drivingSeconds / 60.0).toInt()

                val combinedCost =
                    drivingMinutes + passengerRideMinutes

                if (
                    penaltyMinutes < bestPenalty ||
                    (
                            penaltyMinutes == bestPenalty &&
                                    combinedCost < bestCombinedCost
                            ) ||
                    (
                            penaltyMinutes == bestPenalty &&
                                    combinedCost == bestCombinedCost &&
                                    drivingSeconds < bestDrivingSeconds
                            )
                ) {
                    bestPenalty = penaltyMinutes
                    bestCombinedCost = combinedCost
                    bestPassengerRideMinutes = passengerRideMinutes
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
                        travelSeconds[0][nextIndex + 1]
                    } else {
                        travelSeconds[order.last() + 1][nextIndex + 1]
                    }

                val physicalArrivalMinutes =
                    currentMinutes + (driveSeconds / 60.0).toInt()

                var arrivalMinutes = physicalArrivalMinutes

// If we reach a pickup too early, wait until the
// appropriate pickup window begins.
                if (stop.stopType == RouteStopType.PICKUP) {

                    val scheduled = parseFirstTime(stop.typeTime)

                    if (scheduled != null) {

                        when {
                            stop.typeTime.startsWith("PA", ignoreCase = true) -> {

                                val earliestPickup =
                                    paGroupEarliestPickupByTrip[stop.tripId]
                                        ?: scheduled

                                if (arrivalMinutes < earliestPickup) {
                                    arrivalMinutes = earliestPickup
                                }
                            }

                            stop.typeTime.startsWith("PR", ignoreCase = true) -> {
                                val earliestPickup = scheduled - 15

                                if (arrivalMinutes < earliestPickup) {
                                    arrivalMinutes = earliestPickup
                                }
                            }
                        }
                    }
                }

                var newPenalty = penaltyMinutes

// If this is a shared-destination PA group, prefer collecting
// the whole group before going to their common destination.
                if (
                    stop.stopType == RouteStopType.DROPOFF &&
                    stop.typeTime.startsWith("PA", ignoreCase = true)
                ) {
                    val groupMembers =
                        paGroupMembersByTrip[stop.tripId]

                    if (groupMembers != null) {

                        val uncollectedGroupMembers =
                            groupMembers.count { tripId ->

                                val pickupIndex =
                                    pickupIndexByTrip[tripId]

                                pickupIndex != null &&
                                        pickupIndex !in used
                            }

                        if (uncollectedGroupMembers > 0) {
                            newPenalty +=
                                uncollectedGroupMembers * 10
                        }
                    }
                }

                when {

                    stop.typeTime.startsWith("PR", ignoreCase = true) &&
                            stop.stopType == RouteStopType.PICKUP -> {

                        val scheduled = parseFirstTime(stop.typeTime)

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

                    stop.typeTime.startsWith("PA", ignoreCase = true) &&
                            stop.stopType == RouteStopType.PICKUP -> {

                        val pickupTime = parseFirstTime(stop.typeTime)

                        if (pickupTime != null) {

                            val latestPickup =
                                paGroupLatestPickupByTrip[stop.tripId]
                                    ?: pickupTime

                            if (arrivalMinutes > latestPickup) {
                                newPenalty +=
                                    arrivalMinutes - latestPickup
                            }
                        }
                    }
                    stop.typeTime.startsWith("PA", ignoreCase = true) &&
                            stop.stopType == RouteStopType.DROPOFF -> {

                        val appointmentTime = parseSecondTime(stop.typeTime)

                        if (appointmentTime != null &&
                            arrivalMinutes > appointmentTime
                        ) {
                            newPenalty +=
                                arrivalMinutes - appointmentTime
                        }
                    }
                }
                var newPassengerRideMinutes = passengerRideMinutes
                var newPickupTimes = pickupTimes

                when (stop.stopType) {
                    RouteStopType.PICKUP -> {
                        newPickupTimes =
                            pickupTimes + (stop.tripId to arrivalMinutes)
                    }

                    RouteStopType.DROPOFF -> {
                        val pickupMinute = pickupTimes[stop.tripId]

                        if (pickupMinute != null) {
                            newPassengerRideMinutes +=
                                arrivalMinutes - pickupMinute
                        }
                    }
                }
                search(
                    order = order + nextIndex,
                    used = used + nextIndex,
                    currentMinutes = arrivalMinutes,
                    drivingSeconds = drivingSeconds + driveSeconds,
                    penaltyMinutes = newPenalty,
                    passengerRideMinutes = newPassengerRideMinutes,
                    pickupTimes = newPickupTimes,
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
            passengerRideMinutes = 0,
            pickupTimes = emptyMap(),
            arrivals = emptyList()
        )

        val order = bestOrder ?: return null

        return OptimizedRoute(
            stops = order.map { stops[it] },
            arrivalMinutes = bestArrivals,
            totalDrivingSeconds = bestDrivingSeconds,
            outsideWindowMinutes = bestPenalty,
            passengerRideMinutes = bestPassengerRideMinutes
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
    private fun parseSecondTime(typeTime: String): Int? {

        val matches =
            Regex("""(\d{1,2}):(\d{2})""")
                .findAll(typeTime)
                .toList()

        if (matches.size < 2) {
            return null
        }

        val match = matches[1]

        val hour = match.groupValues[1].toInt()
        val minute = match.groupValues[2].toInt()

        return hour * 60 + minute
    }
}