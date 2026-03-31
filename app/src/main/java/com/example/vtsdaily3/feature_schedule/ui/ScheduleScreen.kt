package com.example.vtsdaily3.feature_schedule.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.PersonSearch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.vtsdaily3.feature_schedule.ui.state.ScheduleUiState
import com.example.vtsdaily3.model.Trip
import com.example.vtsdaily3.model.TripId
import com.example.vtsdaily3.model.TripStatus
import com.example.vtsdaily3.model.TripViewMode
import com.example.vtsdaily3.ui.theme.ActionGreen
import com.example.vtsdaily3.ui.theme.CardHighlight
import com.example.vtsdaily3.ui.theme.FromGrey
import com.example.vtsdaily3.ui.theme.RemovedColor
import com.example.vtsdaily3.ui.theme.VtsError
import com.example.vtsdaily3.ui.theme.VtsWarning
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.core.net.toUri
import com.example.vtsdaily3.feature_schedule.notes.PassengerNotesScreen
import com.example.vtsdaily3.ui.theme.LightGreenCardBackground
import com.example.vtsdaily3.ui.theme.VtsGreen
import com.example.vtsdaily3.ui.theme.SubtleGrey
import com.example.vtsdaily3.feature_clinics.data.ClinicEntry
import com.example.vtsdaily3.feature_clinics.data.ClinicStore
import com.example.vtsdaily3.feature_clinics.domain.findMatchingClinic
import com.example.vtsdaily3.feature_schedule.domain.buildScheduleWarnings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import com.example.vtsdaily3.feature_clinics.domain.resolveClinicCandidateAddress
import com.example.vtsdaily3.feature_lookup.data.InsertTripPrefill
import com.example.vtsdaily3.feature_lookup.data.LookupRow
import com.example.vtsdaily3.feature_lookup.data.LookupStore
import com.example.vtsdaily3.feature_schedule.notes.PassengerNotesStore
import com.example.vtsdaily3.feature_schedule.notes.PassengerResidenceNote
import com.example.vtsdaily3.ui.components.directory.VtsThinDivider



@Composable
fun ScheduleScreen(
    uiState: ScheduleUiState,
    viewModel: ScheduleViewModel,
    onSelectDate: (LocalDate) -> Unit,
    onSelectViewMode: (TripViewMode) -> Unit,
    onMarkTripStatus: (TripId, TripStatus) -> Unit,
    onReinstateTrip: (TripId) -> Unit,
    onRefresh: () -> Unit,
    onPreviousDate: () -> Unit,
    onNextDate: () -> Unit,
    onLookupPassenger: (String) -> Unit,
    onInsertTrip: (Trip) -> Unit,
    onPrefillInsertedTrip: (name: String, tripType: String) -> InsertTripPrefill?
) {
    val context = LocalContext.current

    val formattedSelectedDate = remember(uiState.selectedDate) {
        uiState.selectedDate.format(
            DateTimeFormatter.ofPattern("EEE, MMM d, yyyy")
        )
    }

    var showDatePickerDialog by remember { mutableStateOf(false) }
    var notesTrip by remember { mutableStateOf<Trip?>(null) }
    var pendingClinicAddress by remember { mutableStateOf("") }
    var showAddClinicDialog by remember { mutableStateOf(false) }
    var clinics by remember { mutableStateOf<List<ClinicEntry>>(emptyList()) }
    var showInsertDialog by remember { mutableStateOf(false) }
    var lookupRows by remember { mutableStateOf<List<LookupRow>>(emptyList()) }
    var notes by remember { mutableStateOf<List<PassengerResidenceNote>>(emptyList()) }

    LaunchedEffect(Unit) {
        notes = PassengerNotesStore.getAll(context)
    }

    LaunchedEffect(Unit) {
        clinics = ClinicStore.load(context)
        lookupRows = LookupStore.load(context)
    }


    notesTrip?.let { selectedTrip ->
        PassengerNotesScreen(
            trip = selectedTrip,
            clinics = clinics,
            onClose = { notesTrip = null }
        )
        return
    }

    LaunchedEffect(uiState.tripsForSelectedView, clinics) {
        val warnings = buildScheduleWarnings(
            trips = uiState.tripsForSelectedView,
            clinics = clinics
        )

        warnings.forEach { warning ->
            Log.d("ScheduleWarning", "${warning.tripId}: ${warning.message}")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 8.dp)
    ) {
        Text(
            text = "Schedule",
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 29.dp, bottom = 21.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            color = VtsGreen
        )

        ScheduleHeaderCard(
            selectedDateText = formattedSelectedDate,
            selectedViewMode = uiState.selectedViewMode,
            onPreviousDate = onPreviousDate,
            onNextDate = onNextDate,
            onDateClick = { showDatePickerDialog = true },
            onSelectViewMode = onSelectViewMode
        )
        Spacer(modifier = Modifier.height(2.dp))
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.tripsForSelectedView.isEmpty() -> {
                EmptyScheduleState(
                    viewMode = uiState.selectedViewMode,
                    modifier = Modifier.fillMaxSize()
                )
            }

            else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(2.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                    items(
                        items = uiState.tripsForSelectedView,
                        key = { trip -> trip.id.toString() }
                    ) { trip ->

                        val normalizedTripName = normalizeNameForNotes(trip.name)

                        val hasNote = notes.any {
                            normalizeNameForNotes(it.displayPassengerName) == normalizedTripName
                        }

                        TripCard(
                            trip = trip,
                            hasNote = hasNote,
                            selectedDate = uiState.selectedDate,
                            clinics = clinics,
                            viewMode = uiState.selectedViewMode,
                            onTripActionSelected = { menuAction ->
                                when (menuAction) {
                                    TripMenuAction.COMPLETE -> {
                                        onMarkTripStatus(trip.id, TripStatus.COMPLETED)
                                    }
                                    TripMenuAction.NOSHOW -> {
                                        onMarkTripStatus(trip.id, TripStatus.NOSHOW)
                                    }
                                    TripMenuAction.CANCEL -> {
                                        onMarkTripStatus(trip.id, TripStatus.CANCELLED)
                                    }
                                    TripMenuAction.REMOVE -> {
                                        onMarkTripStatus(trip.id, TripStatus.REMOVED)
                                    }
                                    TripMenuAction.REINSTATE -> {
                                        onReinstateTrip(trip.id)
                                    }
                                }
                            },
                            onLookupPassenger = onLookupPassenger,
                            onPassengerNotes = { selectedTrip ->
                                notesTrip = selectedTrip
                            },
                            onAddClinicRequested = { clinicAddress ->
                                if (clinicAddress.isNotBlank()) {
                                    pendingClinicAddress = clinicAddress
                                    showAddClinicDialog = true
                                }
                            },
                            onAddTripRequested = {
                                showInsertDialog = true
                            }
                        )

                     //*   Spacer(Modifier.height(1.dp)) *//
                      //*  VtsThinDivider() *//
                    }
                }
            }
        }
    }

    if (showDatePickerDialog) {
        ActiveDatePickerDialog(
            availableDates = uiState.availableDates,
            selectedDate = uiState.selectedDate,
            onDismiss = { showDatePickerDialog = false },
            onDateSelected = { selectedDate ->
                showDatePickerDialog = false
                onSelectDate(selectedDate)
            }
        )
    }

    if (showAddClinicDialog) {
        AddClinicDialog(
            initialAddress = pendingClinicAddress,
            onDismiss = {
                showAddClinicDialog = false
                pendingClinicAddress = ""
            },
            onSave = { newClinic ->
                val updatedClinics = clinics + newClinic
                ClinicStore.save(context, updatedClinics)
                clinics = ClinicStore.load(context)
                showAddClinicDialog = false
                pendingClinicAddress = ""
                Toast.makeText(
                    context,
                    "Clinic added",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }

    if (showInsertDialog) {
        InsertTripDialog(
            selectedDate = uiState.selectedDate,
            onDismiss = { showInsertDialog = false },
            onPrefill = onPrefillInsertedTrip,
            onSave = { newTrip ->
                Log.d("INSERT_DEBUG", "Calling onInsertTrip with: $newTrip")
                onInsertTrip(newTrip)
                showInsertDialog = false
            }
        )
    }
}

@Composable
private fun ActiveDatePickerDialog(
    availableDates: List<LocalDate>,
    selectedDate: LocalDate,
    onDismiss: () -> Unit,
    onDateSelected: (LocalDate) -> Unit
) {
    val formatter = remember { DateTimeFormatter.ofPattern("EEE, MMM d, yyyy") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Select Schedule Date",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 360.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(availableDates.sortedDescending()) { date ->
                    val isSelected = date == selectedDate

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onDateSelected(date) },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) {
                                LightGreenCardBackground
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        ),
                        border = BorderStroke(
                            width = if (isSelected) 1.dp else 0.5.dp,
                            color = if (isSelected) {
                                VtsGreen
                            } else {
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                            }
                        )
                    ) {
                        Text(
                            text = date.format(formatter),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = VtsGreen)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun AddClinicDialog(
    initialAddress: String,
    onDismiss: () -> Unit,
    onSave: (ClinicEntry) -> Unit
) {
    var clinicName by remember { mutableStateOf("") }
    var clinicAddress by remember(initialAddress) { mutableStateOf(initialAddress) }
    var clinicPhone by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Clinic",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = clinicName,
                    onValueChange = { clinicName = it },
                    label = { Text("Clinic Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = clinicAddress,
                    onValueChange = { clinicAddress = it },
                    label = { Text("Address") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = clinicPhone,
                    onValueChange = { clinicPhone = it },
                    label = { Text("Phone") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val trimmedName = clinicName.trim()
                    val trimmedAddress = clinicAddress.trim()
                    val trimmedPhone = clinicPhone.trim()

                    if (trimmedName.isNotBlank() && trimmedAddress.isNotBlank()) {
                        onSave(
                            ClinicEntry(
                                name = trimmedName,
                                address = trimmedAddress,
                                phone = trimmedPhone
                            )
                        )
                    }
                }
            ) {
                Text("Save", color = VtsGreen)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = VtsGreen)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun TripCard(
    trip: Trip,
    hasNote: Boolean,
    selectedDate: LocalDate,
    clinics: List<ClinicEntry>,
    viewMode: TripViewMode,
    onTripActionSelected: (TripMenuAction) -> Unit,
    onLookupPassenger: (String) -> Unit,
    onPassengerNotes: (Trip) -> Unit,
    onAddClinicRequested: (String) -> Unit,
    onAddTripRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    var showAddressChooser by remember { mutableStateOf(false) }
    var showPassengerDialog by remember { mutableStateOf(false) }

    val toAddress = trip.toAddress.trim()
    val nameStartOffset = 150.dp

    val clinicAddress = resolveClinicCandidateAddress(
        timeText = trip.time,
        fromAddress = trip.fromAddress,
        toAddress = trip.toAddress
    )

    val matchedClinic = findMatchingClinic(
        address = clinicAddress,
        clinics = clinics
    )

    val clinicPhone = matchedClinic?.phone?.trim()?.takeIf { it.isNotBlank() }

    val isPA = trip.time.contains("PA", ignoreCase = true)
    val isPR = trip.time.contains("PR", ignoreCase = true)
    val shouldHaveClinic = isPA || isPR
    val isMissingClinic = shouldHaveClinic && matchedClinic == null

    val expectedClinicAddress = resolveClinicCandidateAddress(
        timeText = trip.time,
        fromAddress = trip.fromAddress,
        toAddress = trip.toAddress
    )

    val expectedClinicMatch = findMatchingClinic(
        address = expectedClinicAddress,
        clinics = clinics
    )

    val oppositeClinicAddress = when {
        isPR -> trip.toAddress
        isPA -> trip.fromAddress
        else -> ""
    }

    val oppositeClinicMatch = if (
        shouldHaveClinic &&
        expectedClinicMatch == null &&
        oppositeClinicAddress.isNotBlank()
    ) {
        findMatchingClinic(
            address = oppositeClinicAddress,
            clinics = clinics
        )
    } else {
        null
    }

    val effectiveClinic = expectedClinicMatch ?: oppositeClinicMatch
    val isDirectionMismatch = expectedClinicMatch == null && oppositeClinicMatch != null

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = SubtleGrey,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),

        border = BorderStroke(
            0.5.dp,
            Color.Black
            //* MaterialTheme.colorScheme.outline.copy(alpha = 0.7f) *//
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { expanded = !expanded },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(nameStartOffset)
                            .clickable {
                                val phone = trip.phone.trim()
                                if (phone.isNotBlank()) {
                                    context.startActivity(
                                        Intent(Intent.ACTION_DIAL, "tel:$phone".toUri())
                                    )
                                } else {
                                    Toast.makeText(
                                        context,
                                        "No phone number available",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    ) {
                        Text(
                            text = trip.time,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = CardHighlight.copy(alpha = 0.65f),
                            maxLines = 1,
                            overflow = TextOverflow.Clip
                        )
                    }

                    Text(
                        text = trip.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showPassengerDialog = true }
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = {
                        onAddTripRequested()
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AddCircleOutline,
                        contentDescription = "Add Trip",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (showPassengerDialog) {
                val phone = trip.phone.trim()

                AlertDialog(
                    onDismissRequest = { showPassengerDialog = false },
                    title = {
                        Text(
                            text = trip.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = phone.ifBlank { "No phone number available" },
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    },
                    confirmButton = {
                        if (phone.isNotBlank()) {
                            Button(
                                onClick = {
                                    showPassengerDialog = false
                                    context.startActivity(
                                        Intent(Intent.ACTION_DIAL, "tel:$phone".toUri())
                                    )
                                }
                            ) {
                                Text("Call")
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showPassengerDialog = false }
                        ) {
                            Text("Close")
                        }
                    }
                )
            }

            Spacer(Modifier.height(10.dp))

            ClickableAlignedLineV3(
                label = "From:",
                value = trip.fromAddress,
                expanded = expanded,
                onClick = { launchWaze(context, trip.fromAddress) },
                onLongClick = { showAddressChooser = true }
            )

            if (toAddress.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                ClickableAlignedLineV3(
                    label = "To:",
                    value = toAddress,
                    expanded = expanded,
                    onClick = { launchWaze(context, toAddress) },
                    onLongClick = { showAddressChooser = true }
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    when (viewMode) {
                        TripViewMode.ACTIVE -> {
                            SmallPillV3("Complete") {
                                onTripActionSelected(TripMenuAction.COMPLETE)
                            }
                            SmallPillV3("No Show") {
                                onTripActionSelected(TripMenuAction.NOSHOW)
                            }
                            SmallPillV3("Cancel") {
                                onTripActionSelected(TripMenuAction.CANCEL)
                            }
                            SmallPillV3("Remove") {
                                onTripActionSelected(TripMenuAction.REMOVE)
                            }
                        }

                        TripViewMode.COMPLETED -> {
                            SmallPillV3("Reinstate") {
                                onTripActionSelected(TripMenuAction.REINSTATE)
                            }
                            Spacer(Modifier.width(6.dp))
                            OtherReasonBadgeV3("COMPLETED")
                        }

                        TripViewMode.OTHER -> {
                            SmallPillV3("Reinstate") {
                                onTripActionSelected(TripMenuAction.REINSTATE)
                            }

                            val reason = trip.status.otherLabelV3()
                            if (reason.isNotBlank()) {
                                Spacer(Modifier.width(6.dp))
                                OtherReasonBadgeV3(reason)
                            }
                        }
                    }
                }

                val clinicInteractionSource = remember { MutableInteractionSource() }

                Row(
                    modifier = Modifier.padding(start = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(1.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .combinedClickable(
                                interactionSource = clinicInteractionSource,
                                indication = LocalIndication.current,
                                enabled = clinicPhone != null || isMissingClinic,
                                onClick = {
                                    when {
                                        clinicPhone != null -> {
                                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                                data = "tel:$clinicPhone".toUri()
                                            }
                                            context.startActivity(intent)
                                        }
                                        isMissingClinic -> {
                                            Toast.makeText(
                                                context,
                                                "Clinic not found in database",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                },
                                onLongClick = {
                                    when {
                                        clinicPhone != null -> {
                                            effectiveClinic?.let { clinic ->
                                                Toast.makeText(
                                                    context,
                                                    clinic.name,
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                        isMissingClinic -> {
                                            val suggestedAddress = when {
                                                isPR -> trip.fromAddress
                                                isPA -> trip.toAddress
                                                else -> ""
                                            }

                                            if (suggestedAddress.isNotBlank()) {
                                                onAddClinicRequested(suggestedAddress)
                                            }
                                        }
                                    }
                                }

                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LocalHospital,
                            contentDescription = "Call Clinic",
                            tint = when {
                                clinicPhone != null -> MaterialTheme.colorScheme.primary
                                isMissingClinic -> VtsError
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { onLookupPassenger(trip.name) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PersonSearch,
                            contentDescription = "Lookup Passenger",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { onPassengerNotes(trip) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.EditNote,
                            contentDescription = "Passenger Notes",
                            tint = if (hasNote) VtsGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        //* Spacer(Modifier.height(1.dp)) *//

        if (isDirectionMismatch) {
            Log.d(
                "ClinicMatch",
                "Direction mismatch: trip='${trip.name}', time='${trip.time}', " +
                        "expectedSide='${expectedClinicAddress}', oppositeSideMatched='${oppositeClinicMatch.name}'"
            )
        }

        if (showAddressChooser) {
            AlertDialog(
                onDismissRequest = { showAddressChooser = false },
                title = {
                    Text(
                        text = "Navigate",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (trip.fromAddress.isNotBlank()) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showAddressChooser = false
                                        launchWaze(context, trip.fromAddress)
                                    },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = LightGreenCardBackground
                                ),
                                border = BorderStroke(.5.dp, VtsGreen)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Pickup",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = VtsGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = trip.fromAddress,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        if (toAddress.isNotBlank()) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showAddressChooser = false
                                        launchWaze(context, toAddress)
                                    },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = LightGreenCardBackground
                                ),
                                border = BorderStroke(.5.dp, VtsGreen)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Drop-off",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = VtsGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = toAddress,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(
                        onClick = { showAddressChooser = false }
                    ) {
                        Text("Cancel", color = VtsGreen)
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}



@Composable
private fun EmptyScheduleState(
    viewMode: TripViewMode,
    modifier: Modifier = Modifier
) {
    val message = when (viewMode) {
        TripViewMode.ACTIVE -> "No active trips for this date."
        TripViewMode.COMPLETED -> "No completed trips for this date."
        TripViewMode.OTHER -> "No other trips for this date."
    }

    Box(
        modifier = modifier.padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

enum class TripMenuAction(val label: String) {
    COMPLETE("Complete"),
    CANCEL("Cancel"),
    NOSHOW("No Show"),
    REMOVE("Remove"),
    REINSTATE("Reinstate")
}

@Composable
fun ScheduleHeaderCard(
    selectedDateText: String,
    selectedViewMode: TripViewMode,
    onPreviousDate: () -> Unit,
    onNextDate: () -> Unit,
    onDateClick: () -> Unit,
    onSelectViewMode: (TripViewMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = LightGreenCardBackground
        ),
        border = BorderStroke(3.0.dp, VtsGreen),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPreviousDate) {
                    Text(
                        text = "<",
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                Text(
                    text = selectedDateText,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onDateClick() },
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(onClick = onNextDate) {
                    Text(
                        text = ">",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp)) //was 10.dp

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ViewModeButton(
                    label = "Active",
                    selected = selectedViewMode == TripViewMode.ACTIVE,
                    onClick = { onSelectViewMode(TripViewMode.ACTIVE) },
                    modifier = Modifier.weight(1f)
                )

                ViewModeButton(
                    label = "Completed",
                    selected = selectedViewMode == TripViewMode.COMPLETED,
                    onClick = { onSelectViewMode(TripViewMode.COMPLETED) },
                    modifier = Modifier.weight(1f)
                )

                ViewModeButton(
                    label = "Other",
                    selected = selectedViewMode == TripViewMode.OTHER,
                    onClick = { onSelectViewMode(TripViewMode.OTHER) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun ViewModeButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedFill = Color(0xFF4CAF50)
    val selectedText = Color.White
    val unselectedBorder = Color(0xFF81C784)
    val unselectedText = Color(0xFF4CAF50)

    if (selected) {
        Button(
            onClick = onClick,
            modifier = modifier.height(36.dp),
            shape = RoundedCornerShape(24.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = selectedFill,
                contentColor = selectedText
            )
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier.height(36.dp),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.5.dp, unselectedBorder),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.Transparent,
                contentColor = unselectedText
            )
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ClickableAlignedLineV3(
    label: String,
    value: String,
    expanded: Boolean,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    if (value.isBlank()) return

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = FromGrey,
            modifier = Modifier.width(56.dp)
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = if (expanded) 3 else 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                )
        )
    }
}

@Composable
private fun SmallPillV3(
    text: String,
    onClick: () -> Unit
) {
    val base = when (text) {
        "Complete" -> ActionGreen
        "No Show" -> VtsWarning
        "Cancel" -> VtsError
        "Remove" -> RemovedColor
        "Reinstate" -> ActionGreen
        else -> MaterialTheme.colorScheme.primary
    }

    val bg = base.copy(alpha = 0.14f)
    val border = base.copy(alpha = 0.45f)
    val fg = MaterialTheme.colorScheme.onSurface

    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.height(28.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
        border = BorderStroke(1.dp, border),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = bg,
            contentColor = fg
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1
        )
    }
}

@Composable
private fun OtherReasonBadgeV3(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = VtsError
    )
}

private fun launchWaze(context: Context, address: String) {
    val q = Uri.encode(address.trim())
    if (q.isBlank()) return

    fun start(intent: Intent): Boolean {
        if (context !is Activity) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        return try {
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }

    if (start(Intent(Intent.ACTION_VIEW, "waze://?q=$q".toUri()))) return
    if (start(Intent(Intent.ACTION_VIEW, "https://waze.com/ul?q=$q".toUri()))) return
    start(Intent(Intent.ACTION_VIEW, "geo:0,0?q=$q".toUri()))
}


private fun TripStatus.otherLabelV3(): String = when (this) {
    TripStatus.CANCELLED -> "CANCEL"
    TripStatus.NOSHOW -> "NO SHOW"
    TripStatus.REMOVED -> "REMOVED"
    else -> ""
}


private fun normalizeNameForNotes(raw: String): String {
    return raw
        .substringBefore(" (")          // remove (WC) etc
        .lowercase()
        .replace(Regex("[^a-z\\- ]"), "") // keep letters, hyphen, space
        .replace(Regex("\\s+"), " ")   // collapse spaces
        .trim()
}

