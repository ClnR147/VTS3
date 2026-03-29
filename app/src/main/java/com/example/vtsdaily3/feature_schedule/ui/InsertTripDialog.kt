package com.example.vtsdaily3.feature_schedule.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.vtsdaily3.feature_lookup.data.LookupRow
import com.example.vtsdaily3.model.Trip
import com.example.vtsdaily3.model.TripId
import com.example.vtsdaily3.model.TripStatus
import com.example.vtsdaily3.ui.theme.LightGreenCardBackground
import com.example.vtsdaily3.ui.theme.VtsGreen
import java.time.LocalDate

@Composable
fun InsertTripDialog(
    scheduleViewModel: ScheduleViewModel,
    lookupRows: List<LookupRow>,
    selectedDate: LocalDate,
    onDismiss: () -> Unit,
    onSave: (Trip) -> Unit
) {
    var nameInput by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("") }
    var fromInput by remember { mutableStateOf("") }
    var toInput by remember { mutableStateOf("") }
    var timeInput by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("PA") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Trip") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Passenger") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { selectedType = "PA" },
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (selectedType == "PA") LightGreenCardBackground else Color.Transparent
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (selectedType == "PA") VtsGreen else MaterialTheme.colorScheme.outline
                        )
                    ) {
                        Text("PA")
                    }

                    OutlinedButton(
                        onClick = { selectedType = "PR" },
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (selectedType == "PR") LightGreenCardBackground else Color.Transparent
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (selectedType == "PR") VtsGreen else MaterialTheme.colorScheme.outline
                        )
                    ) {
                        Text("PR")
                    }

                    Button(
                        onClick = {
                            val legType = when (selectedType) {
                                "PA" -> ScheduleViewModel.InsertLegType.PA
                                "PR" -> ScheduleViewModel.InsertLegType.PR
                                else -> null
                            }

                            if (nameInput.isBlank() || legType == null) {
                                return@Button
                            }

                            val result = scheduleViewModel.getPrefillForPassenger(
                                passengerName = nameInput,
                                legType = legType
                            )

                            if (result != null) {
                                phoneInput = result.phone
                                fromInput = result.fromAddress
                                toInput = result.toAddress
                            }
                        }
                    ) {
                        Text("Prefill")
                    }
                }

                OutlinedTextField(
                    value = timeInput,
                    onValueChange = { timeInput = it },
                    label = { Text("Time") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phoneInput,
                    onValueChange = { phoneInput = it },
                    label = { Text("Phone") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = fromInput,
                    onValueChange = { fromInput = it },
                    label = { Text("From") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = toInput,
                    onValueChange = { toInput = it },
                    label = { Text("To") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val finalTime = buildString {
                        if (selectedType.isNotBlank()) {
                            append(selectedType)
                            if (timeInput.isNotBlank()) append(" ")
                        }
                        append(timeInput.trim())
                    }

                    val newTrip = Trip(
                        id = TripId.stable(
                            selectedDate,
                            nameInput.trim(),
                            finalTime,
                            fromInput.trim()
                        ),
                        date = selectedDate,
                        time = finalTime,
                        name = nameInput.trim(),
                        phone = phoneInput.trim(),
                        fromAddress = fromInput.trim(),
                        toAddress = toInput.trim(),
                        status = TripStatus.ACTIVE
                    )

                    onSave(newTrip)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}