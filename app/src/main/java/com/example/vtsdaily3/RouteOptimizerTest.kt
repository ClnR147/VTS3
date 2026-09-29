package com.example.vtsdaily3

object RouteOptimizerTest {

    data class Trip(
        val name: String,
        val pickupIndex: Int,
        val dropoffIndex: Int,
        val scheduledMinutes: Int
    )

    data class Result(
        val totalSeconds: Int,
        val stopOrder: List<Int>,
        val pickupArrivalMinutes: Map<Int, Int>,
        val outsideWindowMinutes: Int
    )

    fun findBestRoute(
        travelSeconds: Array<IntArray>,
        trips: List<Trip>,
        startMinutes: Int,
        startIndex: Int
    ): Result {

        var bestSeconds = Int.MAX_VALUE
        var bestLateMinutes = Int.MAX_VALUE
        var bestOrder = emptyList<Int>()
        var bestPickupArrivalMinutes = emptyMap<Int, Int>()


        fun search(
            currentStop: Int?,
            pickedUp: Set<Int>,
            droppedOff: Set<Int>,
            elapsedSeconds: Int,
            order: List<Int>,
            pickupArrivalMinutes: Map<Int, Int>,
            totalLateMinutes: Int
        ) {

            if (droppedOff.size == trips.size) {

                if (
                    totalLateMinutes < bestLateMinutes ||
                    (totalLateMinutes == bestLateMinutes &&
                            elapsedSeconds < bestSeconds)
                ) {
                    bestLateMinutes = totalLateMinutes
                    bestSeconds = elapsedSeconds
                    bestOrder = order
                    bestPickupArrivalMinutes = pickupArrivalMinutes
                }

                return
            }

            trips.forEachIndexed { tripIndex, trip ->

                // Pickup is available if passenger has not been picked up.
                if (tripIndex !in pickedUp) {

                    val driveSeconds =
                        if (currentStop == null)
                            travelSeconds[startIndex][trip.pickupIndex]
                        else
                            travelSeconds[currentStop][trip.pickupIndex]
                    val arrivalMinutes =
                        startMinutes + (elapsedSeconds + driveSeconds) / 60
                    val windowStart = trip.scheduledMinutes - 15
                    val windowEnd = trip.scheduledMinutes + 5

                    val outsideWindowMinutes =
                        when {
                            arrivalMinutes < windowStart ->
                                windowStart - arrivalMinutes

                            arrivalMinutes > windowEnd ->
                                arrivalMinutes - windowEnd

                            else ->
                                0
                        }

                    search(
                        currentStop = trip.pickupIndex,
                        pickedUp = pickedUp + tripIndex,
                        droppedOff = droppedOff,
                        elapsedSeconds = elapsedSeconds + driveSeconds,
                        order = order + trip.pickupIndex,
                        pickupArrivalMinutes =
                            pickupArrivalMinutes + (trip.pickupIndex to arrivalMinutes),
                        totalLateMinutes = totalLateMinutes + outsideWindowMinutes

                    )
                }

                // Drop-off is available only after pickup.
                if (
                    tripIndex in pickedUp &&
                    tripIndex !in droppedOff
                ) {

                    val driveSeconds =
                        travelSeconds[currentStop!!][trip.dropoffIndex]

                    search(
                        currentStop = trip.dropoffIndex,
                        pickedUp = pickedUp,
                        droppedOff = droppedOff + tripIndex,
                        elapsedSeconds = elapsedSeconds + driveSeconds,
                        order = order + trip.dropoffIndex,
                        pickupArrivalMinutes = pickupArrivalMinutes,
                        totalLateMinutes = totalLateMinutes
                    )
                }
            }
        }

        search(
            currentStop = null,
            pickedUp = emptySet(),
            droppedOff = emptySet(),
            elapsedSeconds = 0,
            order = emptyList(),
            pickupArrivalMinutes = emptyMap(),
            totalLateMinutes = 0
        )

        return Result(
            totalSeconds = bestSeconds,
            stopOrder = bestOrder,
            pickupArrivalMinutes = bestPickupArrivalMinutes,
            outsideWindowMinutes = bestLateMinutes
        )
    }
}
