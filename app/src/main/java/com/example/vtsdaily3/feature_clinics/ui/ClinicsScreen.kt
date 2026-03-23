package com.example.vtsdaily3.feature_clinics.ui

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.vtsdaily3.feature_clinics.data.ClinicEntry
import com.example.vtsdaily3.feature_clinics.data.ClinicStore
import com.example.vtsdaily3.ui.components.VtsCard
import com.example.vtsdaily3.ui.components.VtsCardDensity
import com.example.vtsdaily3.ui.components.directory.VtsDirectoryScreenShell
import com.example.vtsdaily3.ui.components.directory.VtsInfoRow
import com.example.vtsdaily3.ui.components.VtsOverflowMenu
import com.example.vtsdaily3.ui.components.VtsSummaryRow
import com.example.vtsdaily3.ui.theme.VtsSpacing
import com.example.vtsdaily3.ui.theme.VtsTextPrimary_Light
import java.io.BufferedReader
import java.io.InputStreamReader

enum class ClinicSortMode {
    NAME,
    ADDRESS
}

@Composable
fun ClinicsScreen() {
    val context = LocalContext.current

    var clinics by remember { mutableStateOf<List<ClinicEntry>>(emptyList()) }

    fun reloadClinics() {
        clinics = ClinicStore.load(context)
    }

    val importClinicsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) {
            Log.d("ClinicsImport", "Import cancelled")
            return@rememberLauncherForActivityResult
        }

        try {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: SecurityException) {
        }

        try {
            val imported = parseClinicsCsv(context, uri)
            ClinicStore.save(context, imported)
            reloadClinics()
            Log.d("ClinicsImport", "Imported ${imported.size} clinics")
        } catch (e: Exception) {
            Log.e("ClinicsImport", "Import failed", e)
        }
    }

    LaunchedEffect(Unit) {
        reloadClinics()
    }

    ClinicsScreenContent(
        clinics = clinics,
        onClinicsChange = { updated ->
            clinics = updated
            ClinicStore.save(context, updated)
        },
        onCallClinic = { clinic ->
            val cleanedPhone = clinic.phone.filter { it.isDigit() || it == '+' }
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$cleanedPhone")
            }
            context.startActivity(intent)
        },
        onImportClinics = {
            importClinicsLauncher.launch(arrayOf("*/*"))
        }
    )
}

@Composable
private fun ClinicsScreenContent(
    clinics: List<ClinicEntry>,
    onClinicsChange: (List<ClinicEntry>) -> Unit,
    onCallClinic: (ClinicEntry) -> Unit,
    onImportClinics: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var sortMode by remember { mutableStateOf(ClinicSortMode.NAME) }
    var menuExpanded by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingClinic by remember { mutableStateOf<ClinicEntry?>(null) }
    var clinicPendingDelete by remember { mutableStateOf<ClinicEntry?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val filteredAndSortedClinics = remember(clinics, searchQuery, sortMode) {
        clinics
            .filter { clinic ->
                val query = searchQuery.trim()
                if (query.isBlank()) {
                    true
                } else {
                    clinic.name.contains(query, ignoreCase = true) ||
                            clinic.address.contains(query, ignoreCase = true)
                }
            }
            .sortedWith(
                when (sortMode) {
                    ClinicSortMode.NAME ->
                        compareBy(String.CASE_INSENSITIVE_ORDER) { it.name }

                    ClinicSortMode.ADDRESS ->
                        compareBy(String.CASE_INSENSITIVE_ORDER) { it.address }
                }
            )
    }

    if (showAddDialog) {
        ClinicEditDialog(
            title = "Add Clinic",
            initialClinic = null,
            onDismiss = { showAddDialog = false },
            onSave = { newClinic ->
                onClinicsChange(clinics + newClinic)
                showAddDialog = false
            }
        )
    }

    if (editingClinic != null) {
        ClinicEditDialog(
            title = "Edit Clinic",
            initialClinic = editingClinic,
            onDismiss = { editingClinic = null },
            onDelete = {
                clinicPendingDelete = editingClinic
                showDeleteConfirm = true
                editingClinic = null
            },
            onSave = { editedClinic ->
                val original = editingClinic ?: return@ClinicEditDialog
                val updated = clinics.map { existing ->
                    if (existing == original) editedClinic else existing
                }
                onClinicsChange(updated)
                editingClinic = null
            }
        )
    }

    if (showDeleteConfirm && clinicPendingDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirm = false
                clinicPendingDelete = null
            },
            title = { Text("Delete Clinic") },
            text = {
                Text("Delete ${clinicPendingDelete!!.name}?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val toDelete = clinicPendingDelete ?: return@TextButton
                        val updated = clinics.filterNot { it == toDelete }
                        onClinicsChange(updated)
                        showDeleteConfirm = false
                        clinicPendingDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = VtsTextPrimary_Light
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        clinicPendingDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = VtsTextPrimary_Light
                    )
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    VtsDirectoryScreenShell(
        title = "Clinics",
        showingDetail = false,
        searchValue = searchQuery,
        onSearchValueChange = { searchQuery = it },
        searchPlaceholder = "Search clinics",
        sortOptions = listOf("Name", "Address"),
        selectedSortOption = when (sortMode) {
            ClinicSortMode.NAME -> "Name"
            ClinicSortMode.ADDRESS -> "Address"
        },
        onSortOptionSelected = { option ->
            sortMode = when (option) {
                "Name" -> ClinicSortMode.NAME
                "Address" -> ClinicSortMode.ADDRESS
                else -> sortMode
            }
        },
        actionSlot = {
            VtsOverflowMenu(
                expanded = menuExpanded,
                onExpandedChange = { menuExpanded = it }
            ) {
                DropdownMenuItem(
                    text = { Text("Add Clinic") },
                    onClick = {
                        menuExpanded = false
                        showAddDialog = true
                    }
                )
                DropdownMenuItem(
                    text = { Text("Import Clinics") },
                    onClick = {
                        menuExpanded = false
                        onImportClinics()
                    }
                )
            }
        },
        isListEmpty = filteredAndSortedClinics.isEmpty(),
        emptyState = {
            if (clinics.isEmpty()) {
                EmptyClinicsState(
                    onAddClinic = { showAddDialog = true }
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = VtsSpacing.xl),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No clinics found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        listContent = {
            ClinicsList(
                clinics = filteredAndSortedClinics,
                onClinicClick = { clinic ->
                    onCallClinic(clinic)
                },
                onClinicLongClick = { clinic ->
                    editingClinic = clinic
                }
            )
        }
    )
}

@Composable
private fun EmptyClinicsState(
    onAddClinic: () -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "No clinics found.",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = "Add a clinic for quick reference.",
            style = MaterialTheme.typography.bodyMedium
        )

        TextButton(
            onClick = onAddClinic,
            colors = ButtonDefaults.textButtonColors(
                contentColor = VtsTextPrimary_Light
            )
        ) {
            Text("Add Clinic")
        }
    }
}

@Composable
private fun ClinicsList(
    clinics: List<ClinicEntry>,
    onClinicClick: (ClinicEntry) -> Unit,
    onClinicLongClick: (ClinicEntry) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(VtsSpacing.sm)
    ) {
        items(clinics) { clinic ->
            ClinicRowCard(
                clinic = clinic,
                onClick = { onClinicClick(clinic) },
                onLongClick = { onClinicLongClick(clinic) }
            )
        }
    }
}

@Composable
private fun ClinicRowCard(
    clinic: ClinicEntry,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Box(
        modifier = Modifier.combinedClickable(
            onClick = onClick,
            onLongClick = onLongClick
        )
    ) {
        VtsCard(
            density = VtsCardDensity.Compact
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                VtsSummaryRow(
                    title = clinic.name,
                    subtitle = null // remove phone from here
                )

                VtsInfoRow(
                    label = "",
                    value = clinic.address
                )

                VtsInfoRow(
                    label = "",
                    value = clinic.phone
                )
            }
        }
    }
}

@Composable
private fun ClinicEditDialog(
    title: String,
    initialClinic: ClinicEntry?,
    onDismiss: () -> Unit,
    onSave: (ClinicEntry) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var name by remember(initialClinic) { mutableStateOf(initialClinic?.name.orEmpty()) }
    var address by remember(initialClinic) { mutableStateOf(initialClinic?.address.orEmpty()) }
    var phone by remember(initialClinic) { mutableStateOf(initialClinic?.phone.orEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(VtsSpacing.sm)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    singleLine = true,
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VtsTextPrimary_Light,
                        unfocusedBorderColor = VtsTextPrimary_Light,
                        focusedLabelColor = VtsTextPrimary_Light,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        cursorColor = VtsTextPrimary_Light
                    )
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VtsTextPrimary_Light,
                        unfocusedBorderColor = VtsTextPrimary_Light,
                        focusedLabelColor = VtsTextPrimary_Light,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        cursorColor = VtsTextPrimary_Light
                    )
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    singleLine = true,
                    label = { Text("Phone") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VtsTextPrimary_Light,
                        unfocusedBorderColor = VtsTextPrimary_Light,
                        focusedLabelColor = VtsTextPrimary_Light,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        cursorColor = VtsTextPrimary_Light
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val trimmedName = name.trim()
                    val trimmedAddress = address.trim()
                    val trimmedPhone = phone.trim()

                    if (trimmedName.isBlank()) return@TextButton

                    onSave(
                        ClinicEntry(
                            name = trimmedName,
                            address = trimmedAddress,
                            phone = trimmedPhone
                        )
                    )
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = VtsTextPrimary_Light
                )
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Row {
                if (onDelete != null) {
                    TextButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = VtsTextPrimary_Light
                        )
                    ) {
                        Text("Delete")
                    }
                }

                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = VtsTextPrimary_Light
                    )
                ) {
                    Text("Cancel")
                }
            }
        }
    )
}

private fun parseClinicsCsv(
    context: android.content.Context,
    uri: Uri
): List<ClinicEntry> {
    val rows = mutableListOf<ClinicEntry>()

    context.contentResolver.openInputStream(uri)?.use { inputStream ->
        BufferedReader(InputStreamReader(inputStream)).use { reader ->
            reader.lineSequence()
                .drop(1) // skip header
                .forEach { line ->
                    val trimmed = line.trim()
                    if (trimmed.isBlank()) return@forEach

                    val parts = parseCsvLine(trimmed)
                    if (parts.size < 3) return@forEach

                    val name = parts[0].trim().trim('"')
                    val address = parts[1].trim().trim('"')
                    val phone = parts[2].trim().trim('"')

                    if (name.isNotBlank()) {
                        rows.add(
                            ClinicEntry(
                                name = name,
                                address = address,
                                phone = phone
                            )
                        )
                    }
                }
        }
    }

    return rows
}

private fun parseCsvLine(line: String): List<String> {
    val result = mutableListOf<String>()
    val current = StringBuilder()
    var inQuotes = false

    line.forEach { char ->
        when (char) {
            '"' -> {
                inQuotes = !inQuotes
            }
            ',' -> {
                if (inQuotes) {
                    current.append(char)
                } else {
                    result.add(current.toString())
                    current.clear()
                }
            }
            else -> current.append(char)
        }
    }

    result.add(current.toString())
    return result
}