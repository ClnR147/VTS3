package com.example.vtsdaily3.ui

import android.net.Uri
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.vtsdaily3.feature_schedule.ui.ScheduleRoute
import com.example.vtsdaily3.feature_lookup.ui.LookupScreen
import com.example.vtsdaily3.feature_contacts.ui.ContactsScreen
import com.example.vtsdaily3.feature_clinics.ui.ClinicsScreen
import com.example.vtsdaily3.ui.navigation.AppDestination
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.vtsdaily3.feature_drivers.ui.DriversScreen


@Composable
fun AppNavHost(
    padding: PaddingValues,
    treeUri: Uri,
    navController: NavHostController
) {
    var pendingLookupPassenger by rememberSaveable { mutableStateOf<String?>(null) }

    NavHost(
        navController = navController,
        startDestination = AppDestination.Schedule.route,
        modifier = Modifier.padding(padding)
    ) {
        composable(AppDestination.Schedule.route) {
            ScheduleRoute(
                onLookupPassenger = { passengerName ->
                    pendingLookupPassenger = passengerName
                    navController.navigate(AppDestination.Lookup.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        composable(AppDestination.Lookup.route) {
            LookupScreen(
                initialPassengerName = pendingLookupPassenger,
                onInitialPassengerNameConsumed = {
                    pendingLookupPassenger = null
                }
            )
        }

        composable(AppDestination.Drivers.route) {
            DriverScreenRoute(treeUri = treeUri)
        }

        composable(AppDestination.Contacts.route) {
            ContactsScreen()
        }

        composable(AppDestination.Clinics.route) {
            ClinicsScreen()
        }
    }

}

@Composable
fun DriverScreenRoute(treeUri: Uri) {
    DriversScreen()
}