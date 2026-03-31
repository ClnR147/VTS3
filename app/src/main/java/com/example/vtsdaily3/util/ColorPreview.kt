package com.example.vtsdaily3.util

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.vtsdaily3.feature_schedule.ui.ScheduleHeaderCard
import com.example.vtsdaily3.model.TripStatus
import com.example.vtsdaily3.model.TripViewMode
import com.example.vtsdaily3.feature_schedule.ui.TripCard
import com.example.vtsdaily3.model.Trip
import com.example.vtsdaily3.model.TripId
import java.time.LocalDate

@Preview(showBackground = true, widthDp = 700)
@Composable
fun CardBackgroundComparisonPreview() {
    MaterialTheme {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Version A
            Column(modifier = Modifier.weight(1f)) {
                Text("Current")

                ScheduleHeaderCard(
                    selectedDateText = "Monday, March 31",
                    selectedViewMode = TripViewMode.ACTIVE,
                    onPreviousDate = {},
                    onNextDate = {},
                    onDateClick = {},
                    onSelectViewMode = {}
                )

                Spacer(Modifier.height(4.dp))

                TripCard(
                    trip = previewTrip(),
                    hasNote = false,
                    selectedDate = LocalDate.now(),
                    clinics = emptyList(),
                    viewMode = TripViewMode.ACTIVE,
                    onTripActionSelected = {},
                    onLookupPassenger = {},
                    onPassengerNotes = {},
                    onAddClinicRequested = {},
                    onAddTripRequested = {}
                )
            }

            // Version B
            Column(modifier = Modifier.weight(1f)) {
                Text("Test")

                ScheduleHeaderCard(
                    selectedDateText = "Monday, March 31",
                    selectedViewMode = TripViewMode.ACTIVE,
                    onPreviousDate = {},
                    onNextDate = {},
                    onDateClick = {},
                    onSelectViewMode = {}
                )

                Spacer(Modifier.height(4.dp))

                TripCard(
                    trip = previewTrip(),
                    hasNote = false,
                    selectedDate = LocalDate.now(),
                    clinics = emptyList(),
                    viewMode = TripViewMode.ACTIVE,
                    onTripActionSelected = {},
                    onLookupPassenger = {},
                    onPassengerNotes = {},
                    onAddClinicRequested = {},
                    onAddTripRequested = {}
                )
            }
        }
    }
}

fun previewTrip(): Trip {
    return Trip(
        id = TripId("preview"),
        date = LocalDate.now(),
        time = "10:30 AM",
        name = "Maria-Lopez",
        phone = "555-1234",
        fromAddress = "123 Main St",
        toAddress = "Dialysis Center",
        status = TripStatus.ACTIVE
    )
}