package com.example.vtsdaily3.feature_schedule.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import java.util.Locale
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.vtsdaily3.feature_schedule.data.CurrentLocation
import com.example.vtsdaily3.feature_schedule.data.CurrentLocationService
import com.example.vtsdaily3.feature_schedule.data.RoutesMatrixService
import com.example.vtsdaily3.feature_schedule.domain.RouteStopType
import com.example.vtsdaily3.feature_schedule.domain.toRouteStops
import com.example.vtsdaily3.model.Trip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.vtsdaily3.feature_schedule.domain.OptimizedRoute
import com.example.vtsdaily3.feature_schedule.domain.RouteOptimizer
import java.time.LocalTime

@Composable
fun RouteHelperDialog(
    trips: List<Trip>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    val routeStops = trips.flatMap { it.toRouteStops() }

    var matrix by remember {
        mutableStateOf<Array<IntArray>?>(null)
    }
    var optimizedRoute by remember {
        mutableStateOf<OptimizedRoute?>(null)
    }

    var currentLocation by remember {
        mutableStateOf<CurrentLocation?>(null)
    }

    var locationPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            locationPermissionGranted = granted
        }

    LaunchedEffect(Unit) {
        if (!locationPermissionGranted) {
            permissionLauncher.launch(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }

    LaunchedEffect(locationPermissionGranted) {
        if (locationPermissionGranted) {
            try {
                currentLocation =
                    CurrentLocationService(context)
                        .getCurrentLocation()
            } catch (e: Exception) {
                errorMessage =
                    "Location error: ${e.message ?: e}"
            }
        }
    }

    LaunchedEffect(routeStops, currentLocation) {

        val startLocation = currentLocation ?: return@LaunchedEffect

        try {
            val addresses = routeStops.map { it.address }

            val result = withContext(Dispatchers.IO) {
                RoutesMatrixService()
                    .getTravelMatrix(
                        addresses = addresses,
                        startLocation = startLocation
                    )
            }

            matrix = result.travelSeconds

            val now = LocalTime.now()
            val startMinutes = now.hour * 60 + now.minute

            optimizedRoute = RouteOptimizer.optimize(
                stops = routeStops,
                travelSeconds = result.travelSeconds,
                startMinutes = startMinutes
            )

        } catch (e: Exception) {
            errorMessage =
                "Routes API error: ${e.message ?: e}"
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Route Helper")
        },
        text = {
            LazyColumn(
                verticalArrangement =
                    Arrangement.spacedBy(12.dp)
            ) {

                item {
                    when {
                        currentLocation != null -> {
                            Text(
                                "Current location: " +
                                        String.format(
                                            Locale.US,
                                            "%.6f, %.6f",
                                            currentLocation!!.latitude,
                                            currentLocation!!.longitude
                                        )
                            )
                        }

                        locationPermissionGranted -> {
                            Text("Getting current location...")
                        }

                        else -> {
                            Text("Waiting for location permission...")
                        }
                    }
                }

                items(routeStops) { stop ->

                    val stopLabel =
                        when (stop.stopType) {
                            RouteStopType.PICKUP ->
                                "PICKUP"

                            RouteStopType.DROPOFF ->
                                "DROPOFF"
                        }

                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "$stopLabel  " +
                                    "${stop.typeTime}  " +
                                    stop.passengerName
                        )

                        Text(
                            text = stop.address,
                            modifier =
                                Modifier.padding(top = 2.dp)
                        )
                    }
                }

                item {
                    val route = optimizedRoute

                    if (route != null) {

                        Text("Suggested route:")

                        route.stops.forEachIndexed { index, stop ->

                            val stopLabel = when (stop.stopType) {
                                RouteStopType.PICKUP -> "PICKUP"
                                RouteStopType.DROPOFF -> "DROPOFF"
                            }

                            val arrival = route.arrivalMinutes[index]

                            val hour = arrival / 60
                            val minute = arrival % 60

                            Text(
                                text = String.format(
                                    Locale.US,
                                    "%d. %02d:%02d  %s  %s",
                                    index + 1,
                                    hour,
                                    minute,
                                    stopLabel,
                                    stop.passengerName
                                ),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        Text(
                            text = String.format(
                                Locale.US,
                                "Driving: %.1f min   PR penalty: %d min",
                                route.totalDrivingSeconds / 60.0,
                                route.outsideWindowMinutes
                            ),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }


                item {
                    when {
                        errorMessage != null -> {
                            Text(errorMessage!!)
                        }

                        matrix == null -> {
                            Text("Getting travel times...")
                        }

                        else -> {
                            Text("Google travel matrix:")

                            matrix!!.forEachIndexed {
                                    origin, row ->

                                val minutes =
                                    row.joinToString("  ") { seconds ->
                                        String.format(
                                            Locale.US,
                                            "%.1f",
                                            seconds / 60.0
                                        )
                                    }

                                Text(
                                    text =
                                        "$origin:  $minutes",
                                    modifier =
                                        Modifier.padding(
                                            top = 2.dp
                                        )
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}