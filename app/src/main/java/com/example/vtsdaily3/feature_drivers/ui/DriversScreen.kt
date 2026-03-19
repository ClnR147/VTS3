package com.example.vtsdaily3.feature_drivers.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import com.example.vtsdaily3.feature_drivers.data.DriverContact
import com.example.vtsdaily3.feature_drivers.data.DriverStore
import com.example.vtsdaily3.feature_drivers.data.DriversFolderPrefs
import com.example.vtsdaily3.ui.theme.VtsTextPrimary_Light
import com.example.vtsdaily3.ui.components.VtsCard
import com.example.vtsdaily3.ui.components.VtsCardDensity
import com.example.vtsdaily3.ui.components.VtsSummaryRow
import com.example.vtsdaily3.ui.components.directory.VtsDirectoryScreenShell
import com.example.vtsdaily3.ui.theme.VtsSpacing

@Composable
fun DriversScreen() {
    val context = LocalContext.current

    var drivers by remember { mutableStateOf<List<DriverContact>>(emptyList()) }

    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult

        context.contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )

        DriversFolderPrefs.saveFolderUri(context, uri.toString())
        drivers = DriverStore.load(context)
    }

    LaunchedEffect(Unit) {
        drivers = DriverStore.load(context)
    }

    DriversScreenContent(
        drivers = drivers,
        onDriversChange = { updated ->
            drivers = updated
            DriverStore.save(context, updated)
        },
        onChooseFolder = { folderPickerLauncher.launch(null) }
    )
}

@Composable
fun DriversScreenPreviewContent(
    drivers: List<DriverContact>
) {
    var localDrivers by remember { mutableStateOf(drivers) }

    DriversScreenContent(
        drivers = localDrivers,
        onDriversChange = { updated ->
            localDrivers = updated
        },
        onChooseFolder = {}
    )
}

@Composable
private fun DriversScreenContent(
    drivers: List<DriverContact>,
    onDriversChange: (List<DriverContact>) -> Unit,
    onChooseFolder: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var sortMode by remember { mutableStateOf(DriverSortMode.NAME) }
    var driverPendingDelete by remember { mutableStateOf<DriverContact?>(null) }

    var showAddDialog by remember { mutableStateOf(false) }
    var editingDriver by remember { mutableStateOf<DriverContact?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val filteredAndSortedDrivers = remember(drivers, searchQuery, sortMode) {
        drivers
            .filter { driver ->
                val query = searchQuery.trim()
                if (query.isBlank()) {
                    true
                } else {
                    driver.name.contains(query, ignoreCase = true) ||
                            driver.phone.contains(query, ignoreCase = true) ||
                            driver.vanNumber.contains(query, ignoreCase = true)
                }
            }
            .sortedWith(
                when (sortMode) {
                    DriverSortMode.NAME -> compareBy(String.CASE_INSENSITIVE_ORDER) { it.name }
                    DriverSortMode.VAN -> compareBy(String.CASE_INSENSITIVE_ORDER) { it.vanNumber }
                }
            )
    }

    if (showAddDialog) {
        DriverEditDialog(
            title = "Add Driver",
            initialDriver = null,
            onDismiss = { showAddDialog = false },
            onSave = { newDriver ->
                onDriversChange(drivers + newDriver)
                showAddDialog = false
            }
        )
    }

    if (editingDriver != null) {
        DriverEditDialog(
            title = "Edit Driver",
            initialDriver = editingDriver,
            onDismiss = { editingDriver = null },
            onDelete = {
                driverPendingDelete = editingDriver
                showDeleteConfirm = true
                editingDriver = null
            },
            onSave = { editedDriver ->
                val original = editingDriver ?: return@DriverEditDialog
                val updated = drivers.map { existing ->
                    if (existing == original) editedDriver else existing
                }
                onDriversChange(updated)
                editingDriver = null
            }
        )
    }

    if (showDeleteConfirm && driverPendingDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirm = false
                driverPendingDelete = null
            },
            title = { Text("Delete Driver") },
            text = {
                Text("Delete ${driverPendingDelete!!.name}?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val toDelete = driverPendingDelete ?: return@TextButton
                        val updated = drivers.filterNot { it == toDelete }
                        onDriversChange(updated)
                        showDeleteConfirm = false
                        driverPendingDelete = null
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
                        driverPendingDelete = null
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
        title = "Drivers",
        showingDetail = false,
        searchValue = searchQuery,
        onSearchValueChange = { searchQuery = it },
        searchPlaceholder = "Search drivers",
        sortBar = {
            DriverSortBar(
                sortMode = sortMode,
                onSortSelected = { sortMode = it }
            )
        },
        actionSlot = {
            FloatingActionButton(
                onClick = { showAddDialog = true }
            ) {
                Icon(
                    imageVector = Icons.Filled.PersonAdd,
                    contentDescription = "Add driver",
                    tint = VtsTextPrimary_Light
                )
            }
        },
        isListEmpty = filteredAndSortedDrivers.isEmpty(),
        emptyState = {
            if (drivers.isEmpty()) {
                EmptyDriversState(
                    onChooseFolder = onChooseFolder,
                    onAddDriver = { showAddDialog = true }
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = VtsSpacing.xl),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No drivers found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        listContent = {
            DriversList(
                drivers = filteredAndSortedDrivers,
                onDriverClick = { driver ->
                    editingDriver = driver
                }
            )
        }
    )
}

@Composable
private fun DriverSortBar(
    sortMode: DriverSortMode,
    onSortSelected: (DriverSortMode) -> Unit
) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        TextButton(
            onClick = { onSortSelected(DriverSortMode.NAME) },
            colors = ButtonDefaults.textButtonColors(
                contentColor = VtsTextPrimary_Light
            )
        ) {
            Text(
                text = if (sortMode == DriverSortMode.NAME) "Name ✓" else "Name"
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        TextButton(
            onClick = { onSortSelected(DriverSortMode.VAN) },
            colors = ButtonDefaults.textButtonColors(
                contentColor = VtsTextPrimary_Light
            )
        ) {
            Text(
                text = if (sortMode == DriverSortMode.VAN) "Van ✓" else "Van"
            )
        }
    }
}

@Composable
private fun EmptyDriversState(
    onChooseFolder: () -> Unit,
    onAddDriver: () -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "No driver data found.",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = "Choose the folder that contains drivers.json, or add drivers manually.",
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            text = "Tap here to choose folder",
            style = MaterialTheme.typography.bodyLarge,
            color = VtsTextPrimary_Light,
            modifier = Modifier.clickable { onChooseFolder() }
        )

        TextButton(
            onClick = onAddDriver,
            colors = ButtonDefaults.textButtonColors(
                contentColor = VtsTextPrimary_Light
            )
        ) {
            Text("Add Driver")
        }
    }
}

@Composable
private fun DriversList(
    drivers: List<DriverContact>,
    onDriverClick: (DriverContact) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(drivers) { driver ->
            DriverRowCard(
                driver = driver,
                onClick = { onDriverClick(driver) }
            )
        }
    }
}

@Composable
fun DriverRowCard(
    driver: DriverContact,
    onClick: () -> Unit
) {
    VtsCard(
        onClick = onClick,
        density = VtsCardDensity.Compact
    ) {
        VtsSummaryRow(
            title = driver.name,
            subtitle = driver.phone,
            trailingText = driver.vanNumber
        )
    }
}

@Composable
private fun DriverEditDialog(
    title: String,
    initialDriver: DriverContact?,
    onDismiss: () -> Unit,
    onSave: (DriverContact) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var name by remember(initialDriver) { mutableStateOf(initialDriver?.name.orEmpty()) }
    var phone by remember(initialDriver) { mutableStateOf(initialDriver?.phone.orEmpty()) }
    var vanNumber by remember(initialDriver) { mutableStateOf(initialDriver?.vanNumber.orEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
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

                OutlinedTextField(
                    value = vanNumber,
                    onValueChange = { vanNumber = it },
                    singleLine = true,
                    label = { Text("Van") },
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
                    val trimmedPhone = phone.trim()
                    val trimmedVan = vanNumber.trim()

                    if (trimmedName.isBlank()) return@TextButton

                    onSave(
                        DriverContact(
                            name = trimmedName,
                            phone = trimmedPhone,
                            vanNumber = trimmedVan
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