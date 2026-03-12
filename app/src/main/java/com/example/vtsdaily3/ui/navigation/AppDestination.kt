package com.example.vtsdaily3.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

sealed class AppDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    data object Schedule : AppDestination(
        route = "schedule",
        label = "Schedule",
        icon = Icons.Filled.CalendarMonth
    )

    data object Lookup : AppDestination(
        route = "lookup",
        label = "Lookup",
        icon = Icons.Filled.Search
    )

    data object Drivers : AppDestination(
        route = "drivers",
        label = "Drivers",
        icon = Icons.Filled.DirectionsCar
    )

    data object Contacts : AppDestination(
        route = "contacts",
        label = "Contacts",
        icon = Icons.Filled.Contacts
    )

    data object Clinics : AppDestination(
        route = "clinics",
        label = "Clinics",
        icon = Icons.Filled.LocalHospital
    )
}

val bottomDestinations = listOf(
    AppDestination.Schedule,
    AppDestination.Lookup,
    AppDestination.Drivers,
    AppDestination.Contacts,
    AppDestination.Clinics
)