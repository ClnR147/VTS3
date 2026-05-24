package com.example.vtsdaily3.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.vtsdaily3.feature_schedule.notes.PassengerResidenceNote
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun PassengerNotesBrowserScreen(
    notes: List<PassengerResidenceNote>,
    onClose: () -> Unit
) {
    var selectedNote by remember { mutableStateOf<PassengerResidenceNote?>(null) }

    if (selectedNote != null) {
        PassengerNoteDetailScreen(
            note = selectedNote!!,
            onBack = { selectedNote = null }
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Text(
                text = "All Notes",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(notes) { note ->
                    PassengerNoteSummaryCard(
                        note = note,
                        onClick = { selectedNote = note }
                    )
                }
            }
        }
    }
}

@Composable
fun PassengerNoteSummaryCard(
    note: PassengerResidenceNote,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = note.displayPassengerName,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = note.displayResidenceAddress,
                style = MaterialTheme.typography.bodyMedium
            )

            if (note.gateCode.isNotBlank()) {
                Text("Gate: ${note.gateCode}")
            }

            if (!note.correctedPhone.isNullOrBlank()) {
                Text("Correct Phone: ${note.correctedPhone}")
            }

            if (note.noteText.isNotBlank()) {
                Text(
                    text = note.noteText,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun PassengerNoteDetailScreen(
    note: PassengerResidenceNote,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(onClick = onBack) {
            Text("Back")
        }

        Text(
            text = note.displayPassengerName,
            style = MaterialTheme.typography.titleLarge
        )

        Text("Residence:")
        Text(note.displayResidenceAddress)

        Text("Side: ${note.residenceSide}")

        if (note.gateCode.isNotBlank()) {
            Text("Gate Code: ${note.gateCode}")
        }

        if (!note.correctedPhone.isNullOrBlank()) {
            Text("Correct Phone: ${note.correctedPhone}")
        }

        if (note.noteText.isNotBlank()) {
            Text("Note:")
            Text(note.noteText)
        }
    }
}
