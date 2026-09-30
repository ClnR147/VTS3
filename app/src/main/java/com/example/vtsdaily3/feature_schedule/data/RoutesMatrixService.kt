package com.example.vtsdaily3.feature_schedule.data

import com.example.vtsdaily3.BuildConfig
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import com.example.vtsdaily3.feature_schedule.data.CurrentLocation

class RoutesMatrixService {

    data class MatrixResult(
        val travelSeconds: Array<IntArray>
    )

    fun getTravelMatrix(
        addresses: List<String>,
        startLocation: CurrentLocation? = null
    ): MatrixResult {

        require(addresses.isNotEmpty())

        val url = URL(
            "https://routes.googleapis.com/distanceMatrix/v2:computeRouteMatrix"
        )

        val connection = url.openConnection() as HttpURLConnection

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
            "originIndex,destinationIndex,duration,status,condition"
        )

        val origins = JSONArray()
        val destinations = JSONArray()

        if (startLocation != null) {

            val startWaypoint = JSONObject()
                .put(
                    "waypoint",
                    JSONObject()
                        .put(
                            "location",
                            JSONObject()
                                .put(
                                    "latLng",
                                    JSONObject()
                                        .put("latitude", startLocation.latitude)
                                        .put("longitude", startLocation.longitude)
                                )
                        )
                )

            origins.put(startWaypoint)
            destinations.put(startWaypoint)
        }

        addresses.forEach { address ->

            val waypoint = JSONObject()
                .put(
                    "waypoint",
                    JSONObject()
                        .put(
                            "address",
                            address
                        )
                )

            origins.put(waypoint)
            destinations.put(waypoint)
        }

        val requestBody = JSONObject()
            .put("origins", origins)
            .put("destinations", destinations)
            .put("travelMode", "DRIVE")
            .put("routingPreference", "TRAFFIC_AWARE_OPTIMAL")

        connection.outputStream.use { output ->
            output.write(
                requestBody.toString().toByteArray(Charsets.UTF_8)
            )
        }

        val responseCode = connection.responseCode

        val responseText =
            if (responseCode in 200..299) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader()?.use { it.readText() }
                    ?: "Unknown Routes API error"
            }

        connection.disconnect()

        if (responseCode !in 200..299) {
            error(
                "Routes API HTTP $responseCode\n$responseText"
            )
        }

        val elements = JSONArray(responseText)

        val matrixSize =
            addresses.size + if (startLocation != null) 1 else 0

        val travelSeconds =
            Array(matrixSize) {
                IntArray(matrixSize)
            }

        for (i in 0 until elements.length()) {

            val element = elements.getJSONObject(i)

            val originIndex =
                element.getInt("originIndex")

            val destinationIndex =
                element.getInt("destinationIndex")

            val duration =
                element.optString("duration")

            travelSeconds[originIndex][destinationIndex] =
                parseDurationSeconds(duration)
        }

        return MatrixResult(
            travelSeconds = travelSeconds
        )
    }

    private fun parseDurationSeconds(
        duration: String
    ): Int {

        if (duration.isBlank()) {
            return 0
        }

        return duration
            .removeSuffix("s")
            .toDouble()
            .toInt()
    }
}