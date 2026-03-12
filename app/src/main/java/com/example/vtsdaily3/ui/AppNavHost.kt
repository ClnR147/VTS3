package com.example.vtsdaily3.ui

import android.net.Uri
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.vtsdaily3.feature_schedule.ui.ScheduleRoute
import com.example.vtsdaily3.feature_lookup.ui.LookupScreen
//import com.example.vtsdaily3.feature_drivers.ui.DriverScreenRoute
//import com.example.vtsdaily3.feature_contacts.ui.ContactScreen
//import com.example.vtsdaily3.feature_clinics.ui.ClinicScreenRoute
import com.example.vtsdaily3.ui.navigation.AppDestination

@Composable
fun AppNavHost(
    padding: PaddingValues,
    treeUri: Uri,
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = AppDestination.Schedule.route,
        modifier = Modifier.padding(padding)
    ) {
        composable(AppDestination.Schedule.route) {
            ScheduleRoute()
        }

        composable(AppDestination.Lookup.route) {
            LookupScreen()
        }

        composable(AppDestination.Drivers.route) {
            DriverScreenRoute(treeUri = treeUri)
        }

        composable(AppDestination.Contacts.route) {
            ContactScreen()
        }

        composable(AppDestination.Clinics.route) {
            ClinicScreenRoute(treeUri = treeUri)
        }
    }
}

@Composable
fun ClinicScreenRoute(treeUri: Uri) {
    TODO("Not yet implemented")
}

@Composable
fun ContactScreen() {
    TODO("Not yet implemented")
}

@Composable
fun DriverScreenRoute(treeUri: Uri) {
    TODO("Not yet implemented")
}