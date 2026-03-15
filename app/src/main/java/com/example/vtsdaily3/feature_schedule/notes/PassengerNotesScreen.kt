package com.example.vtsdaily3.feature_schedule.notes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.vtsdaily3.model.Trip

@Composable
fun PassengerNotesScreen(
    trip: Trip,
    onClose: () -> Unit
) {
    val context = LocalContext.current

    val puAddress = trip.fromAddress.trim()
    val doAddress = trip.toAddress.trim()

    val puKey = remember(trip.name, puAddress) {
        buildPassengerResidenceKey(trip.name, puAddress)
    }
    val doKey = remember(trip.name, doAddress) {
        buildPassengerResidenceKey(trip.name, doAddress)
    }

    val existingPu = remember(trip.name, puAddress) {
        PassengerNotesStore.get(context, puKey)
    }
    val existingDo = remember(trip.name, doAddress) {
        PassengerNotesStore.get(context, doKey)
    }

    val initialSide = when {
        existingPu != null -> ResidenceSide.PU
        existingDo != null -> ResidenceSide.DO
        else -> ResidenceSide.PU
    }

    val initialRecord = existingPu ?: existingDo

    var selectedSide by remember(trip.name, puAddress, doAddress) {
        mutableStateOf(initialSide)
    }
    var gateCode by remember(trip.name, puAddress, doAddress) {
        mutableStateOf(initialRecord?.gateCode ?: "")
    }
    var noteText by remember(trip.name, puAddress, doAddress) {
        mutableStateOf(initialRecord?.noteText ?: "")
    }

    LaunchedEffect(selectedSide) {
        val selectedKey = when (selectedSide) {
            ResidenceSide.PU -> puKey
            ResidenceSide.DO -> doKey
        }

        val selectedRecord = PassengerNotesStore.get(context, selectedKey)

        if (selectedRecord != null) {
            gateCode = selectedRecord.gateCode
            noteText = selectedRecord.noteText
        } else {
            gateCode = ""
            noteText = ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = trip.name,
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "PU: $puAddress",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "DO: $doAddress",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Residence",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.selectable(
                    selected = selectedSide == ResidenceSide.PU,
                    onClick = { selectedSide = ResidenceSide.PU }
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedSide == ResidenceSide.PU,
                    onClick = { selectedSide = ResidenceSide.PU }
                )
                Text("PU")
            }

            Row(
                modifier = Modifier.selectable(
                    selected = selectedSide == ResidenceSide.DO,
                    onClick = { selectedSide = ResidenceSide.DO }
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedSide == ResidenceSide.DO,
                    onClick = { selectedSide = ResidenceSide.DO }
                )
                Text("DO")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = gateCode,
            onValueChange = { gateCode = it },
            label = { Text("Gate Code") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = noteText,
            onValueChange = { noteText = it },
            label = { Text("Notes") },
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            maxLines = 8
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    val selectedAddress = when (selectedSide) {
                        ResidenceSide.PU -> puAddress
                        ResidenceSide.DO -> doAddress
                    }

                    val note = PassengerResidenceNote(
                        recordKey = buildPassengerResidenceKey(trip.name, selectedAddress),
                        passengerKey = normalizePassengerNameForNotes(trip.name),
                        displayPassengerName = trip.name,
                        residenceAddressKey = normalizeAddressForNotes(selectedAddress),
                        displayResidenceAddress = selectedAddress,
                        residenceSide = selectedSide,
                        gateCode = gateCode,
                        noteText = noteText
                    )

                    PassengerNotesStore.put(context, note)
                    onClose()
                }
            ) {
                Text("Save")
            }

            OutlinedButton(
                onClick = onClose
            ) {
                Text("Cancel")
            }
        }
    }
}
