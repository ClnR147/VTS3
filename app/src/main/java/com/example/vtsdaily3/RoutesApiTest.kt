package com.example.vtsdaily3

import java.net.HttpURLConnection
import java.net.URL
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

object RoutesApiTest {


    data class MatrixElement(
        val originIndex: Int,
        val destinationIndex: Int,
        val distanceMeters: Int = 0,
        val duration: String = "",
        val status: Map<String, Any> = emptyMap()
    )

    private fun durationToSeconds(duration: String): Int {
        return duration
            .removeSuffix("s")
            .toDoubleOrNull()
            ?.toInt()
            ?: 0
    }
    fun testRouteMatrix(): String {

        val url = URL(
            "https://routes.googleapis.com/distanceMatrix/v2:computeRouteMatrix"
        )

        val connection =
            url.openConnection() as HttpURLConnection

        connection.requestMethod = "POST"
        connection.doOutput = true

        connection.setRequestProperty(
            "Content-Type",
            "application/json"
        )

        connection.setRequestProperty(
            "X-Goog-Api-Key",
            BuildConfig.MAPS_API_KEY
        )

        connection.setRequestProperty(
            "X-Goog-FieldMask",
            "originIndex,destinationIndex,duration,distanceMeters,status"
        )

        val addresses = listOf(
            "301 N R St, Lompoc, CA 93436",
            "513 N G St, Lompoc, CA 93436",
            "1225 N H St, Lompoc, CA 93436",
            "1428 W North Ave, Lompoc, CA 93436",
            "425 W Central Ave, Lompoc, CA 93436",
            "804 W Lime Ave, Lompoc, CA 93436",
            "521 E Ocean Ave, Lompoc, CA 93436",
            "910 W Chestnut Ave, Lompoc, CA 93436",
            "416 W North Ave, Lompoc, CA 93436"
        )

        val stopDescriptions = mapOf(
            0 to "PICKUP  - Kimberly Huber",
            1 to "DROPOFF - Kimberly Huber",
            2 to "PICKUP  - Martha Santos (WC)",
            3 to "DROPOFF - Martha Santos (WC)",
            4 to "PICKUP  - Delia Clay",
            5 to "DROPOFF - Delia Clay",
            6 to "PICKUP  - Christian Mariscal",
            7 to "DROPOFF - Christian Mariscal"
        )

        val waypoints =
            addresses.joinToString(",") { address ->
                """
                {
                  "waypoint": {
                    "address": "$address"
                  }
                }
                """.trimIndent()
            }

        val requestBody =
            """
            {
              "origins": [
                $waypoints
              ],
              "destinations": [
                $waypoints
              ],
              "travelMode": "DRIVE",
              "routingPreference": "TRAFFIC_AWARE_OPTIMAL"
            }
            """.trimIndent()

        connection.outputStream.use { output ->
            output.write(
                requestBody.toByteArray(Charsets.UTF_8)
            )
        }

        val responseCode =
            connection.responseCode

        val stream =
            if (responseCode in 200..299)
                connection.inputStream
            else
                connection.errorStream

        val response =
            stream.bufferedReader().use {
                it.readText()
            }

        connection.disconnect()

        if (responseCode !in 200..299) {
            return "HTTP $responseCode\n$response"
        }

        val elements =
            Gson().fromJson(
                response,
                Array<MatrixElement>::class.java
            )

        val travelSeconds =
            Array(addresses.size) {
                IntArray(addresses.size)
            }

        for (element in elements) {
            travelSeconds[element.originIndex][element.destinationIndex] =
                durationToSeconds(element.duration)
        }

// optimizer starts HERE

        val trips = listOf(
            RouteOptimizerTest.Trip(
                "Kimberly Huber",
                pickupIndex = 0,
                dropoffIndex = 1,
                scheduledMinutes = 16 * 60
            ),
            RouteOptimizerTest.Trip(
                "Martha Santos (WC)",
                pickupIndex = 2,
                dropoffIndex = 3,
                scheduledMinutes = 16 * 60 + 15
            ),
            RouteOptimizerTest.Trip(
                "Delia Clay",
                pickupIndex = 4,
                dropoffIndex = 5,
                scheduledMinutes = 16 * 60 + 30
            ),
            RouteOptimizerTest.Trip(
                "Christian Mariscal",
                pickupIndex = 6,
                dropoffIndex = 7,
                scheduledMinutes = 16 * 60 + 45
            )
        )

        val bestRoute =
            RouteOptimizerTest.findBestRoute(
                travelSeconds,
                trips,
                startMinutes = 15 * 60 + 55,
                startIndex = 8
            )

        for (element in elements) {
            travelSeconds[element.originIndex][element.destinationIndex] =
                durationToSeconds(element.duration)
        }

        val result = StringBuilder()

        result.appendLine("HTTP $responseCode")
        result.appendLine()
        result.appendLine("TRAVEL TIME MATRIX (minutes)")
        result.appendLine()

        result.append("     ")

        for (destination in addresses.indices) {
            result.append("%5d".format(destination))
        }

        result.appendLine()

        for (origin in addresses.indices) {

            result.append("%3d  ".format(origin))

            for (destination in addresses.indices) {

                val seconds =
                    travelSeconds[origin][destination]

                val minutes =
                    seconds / 60.0

                result.append(
                    "%5.1f".format(minutes)
                )
            }

            result.appendLine()
        }

        result.appendLine()
        result.appendLine("LOCATIONS")

        addresses.forEachIndexed { index, address ->
            result.appendLine("$index = $address")
        }
        result.appendLine()
        result.appendLine("BEST ROUTE - PR WINDOW + DRIVING TIME")
        result.appendLine()

        bestRoute.stopOrder.forEachIndexed { number, stopIndex ->

            val trip = trips.first {
                it.pickupIndex == stopIndex || it.dropoffIndex == stopIndex
            }

            val description =
                if (stopIndex == trip.pickupIndex) {

                    val scheduledHours = trip.scheduledMinutes / 60
                    val scheduledMins = trip.scheduledMinutes % 60
                    val scheduledTime =
                        "%02d:%02d".format(scheduledHours, scheduledMins)

                    val arrivalMinutes =
                        bestRoute.pickupArrivalMinutes[stopIndex]!!

                    val arrivalHours = arrivalMinutes / 60
                    val arrivalMins = arrivalMinutes % 60
                    val arrivalTime =
                        "%02d:%02d".format(arrivalHours, arrivalMins)

                    val windowStart = trip.scheduledMinutes - 15
                    val windowEnd = trip.scheduledMinutes + 5

                    val timing =
                        when {
                            arrivalMinutes < windowStart ->
                                "${windowStart - arrivalMinutes} min before window"

                            arrivalMinutes > windowEnd ->
                                "${arrivalMinutes - windowEnd} min after window"

                            else ->
                                "in window"
                        }

                    "PICKUP  - ${trip.name} - PR $scheduledTime - arrive $arrivalTime ($timing)"
                } else {
                    "DROPOFF - ${trip.name}"
                }

            result.appendLine(
                "${number + 1}. $description - ${addresses[stopIndex]}"
            )
        }

        result.appendLine()
        result.appendLine("PR window penalty: ${bestRoute.outsideWindowMinutes} minutes")
        result.appendLine("Total driving time: %.1f minutes".format(bestRoute.totalSeconds / 60.0)
        )
        return result.toString()
    }
}