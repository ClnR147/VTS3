package com.example.vtsdaily3.feature_drivers.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.vtsdaily3.feature_drivers.data.DriverContact
import com.example.vtsdaily3.feature_drivers.data.DriverStore
import com.example.vtsdaily3.feature_drivers.data.DriversFolderPrefs
import com.example.vtsdaily3.ui.template.VtsScreenTemplate
import com.example.vtsdaily3.ui.theme.VtsTextPrimary_Light
import androidx.core.net.toUri
import com.example.vtsdaily3.ui.theme.Vts3DailyTheme
import com.example.vtsdaily3.ui.components.directory.VtsInfoRow
import com.example.vtsdaily3.ui.components.VtsCard
import com.example.vtsdaily3.ui.components.VtsCardDensity
import com.example.vtsdaily3.ui.components.VtsSummaryRow
import com.example.vtsdaily3.ui.components.directory.VtsInfoRow

@Composable
fun DriversScreen() {
    val context = LocalContext.current

    var drivers by remember { mutableStateOf<List<DriverContact>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var sortMode by remember { mutableStateOf(DriverSortMode.NAME) }
    var selectedDriver by remember { mutableStateOf<DriverContact?>(null) }

    var showAddDialog by remember { mutableStateOf(false) }
    var editingDriver by remember { mutableStateOf<DriverContact?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

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
                val updated = drivers + newDriver
                drivers = updated
                DriverStore.save(context, updated)
                showAddDialog = false
            }
        )
    }

    if (editingDriver != null) {
        DriverEditDialog(
            title = "Edit Driver",
            initialDriver = editingDriver,
            onDismiss = { editingDriver = null },
            onSave = { editedDriver ->
                val original = editingDriver ?: return@DriverEditDialog
                val updated = drivers.map { existing ->
                    if (existing == original) editedDriver else existing
                }
                drivers = updated
                DriverStore.save(context, updated)

                if (selectedDriver == original) {
                    selectedDriver = editedDriver
                }

                editingDriver = null
            }
        )
    }

    if (showDeleteConfirm && selectedDriver != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Driver") },
            text = {
                Text("Delete ${selectedDriver!!.name}?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val toDelete = selectedDriver ?: return@TextButton
                        val updated = drivers.filterNot { it == toDelete }
                        drivers = updated
                        DriverStore.save(context, updated)
                        selectedDriver = null
                        showDeleteConfirm = false
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
                    onClick = { showDeleteConfirm = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = VtsTextPrimary_Light
                    )
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (selectedDriver != null) {
        DriverDetailScreen(
            driver = selectedDriver!!,
            onBack = { selectedDriver = null },
            onCallDriver = { phone ->
                launchDialer(context, phone)
            },
            onEditDriver = {
                editingDriver = selectedDriver
            },
            onDeleteDriver = {
                showDeleteConfirm = true
            }
        )
        return
    }

    VtsScreenTemplate(
        title = "Drivers",
        showControls = true,
        dropdown = {
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
        searchBar = {
            DriverSearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it }
            )
        },
        sortBar = {
            DriverSortBar(
                sortMode = sortMode,
                onSortSelected = { sortMode = it }
            )
        }
    ) {
        if (drivers.isEmpty()) {
            EmptyDriversState(
                onChooseFolder = { folderPickerLauncher.launch(null) },
                onAddDriver = { showAddDialog = true }
            )
        } else {
            DriversList(
                drivers = filteredAndSortedDrivers,
                onDriverClick = { driver ->
                    selectedDriver = driver
                }
            )
        }
    }
}

@Composable
private fun DriverSearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        label = { Text("Search drivers") },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = VtsTextPrimary_Light,
            unfocusedBorderColor = VtsTextPrimary_Light,
            focusedLabelColor = VtsTextPrimary_Light,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            cursorColor = VtsTextPrimary_Light
        )
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
private fun DriverRowCard(
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
private fun DriverDetailScreen(
    driver: DriverContact,
    onBack: () -> Unit,
    onCallDriver: (String) -> Unit,
    onEditDriver: () -> Unit,
    onDeleteDriver: () -> Unit
) {
    VtsScreenTemplate(
        title = "Drivers",
        showControls = false
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextButton(
                onClick = onBack,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = VtsTextPrimary_Light
                )
            ) {
                Text("Back")
            }

            VtsCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = driver.name,
                        style = MaterialTheme.typography.headlineSmall
                    )

                    Row {
                        IconButton(
                            onClick = { onCallDriver(driver.phone) }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Call,
                                contentDescription = "Call driver",
                                tint = VtsTextPrimary_Light
                            )
                        }

                        IconButton(
                            onClick = onEditDriver
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Edit driver",
                                tint = VtsTextPrimary_Light
                            )
                        }

                        IconButton(
                            onClick = onDeleteDriver
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Delete driver",
                                tint = VtsTextPrimary_Light
                            )
                        }
                    }
                }

                ThinDriverDivider()

                VtsInfoRow(
                    label = "Phone",
                    value = driver.phone
                )

                VtsInfoRow(
                    label = "Van",
                    value = driver.vanNumber
                )
            }
        }
    }
}

@Composable
private fun DriverEditDialog(
    title: String,
    initialDriver: DriverContact?,
    onDismiss: () -> Unit,
    onSave: (DriverContact) -> Unit
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
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = VtsTextPrimary_Light
                )
            ) {
                Text("Cancel")
            }
        }
    )
}


@Composable
private fun ThinDriverDivider() {
    HorizontalDivider(
        modifier = Modifier.fillMaxWidth(),
        thickness = 1.dp
    )
}

private fun launchDialer(context: Context, phone: String) {
    val cleaned = phone.trim()

    if (cleaned.isBlank()) {
        Toast.makeText(context, "No phone number available", Toast.LENGTH_SHORT).show()
        return
    }

    val intent = Intent(Intent.ACTION_DIAL).apply {
        data = "tel:$cleaned".toUri()
    }

    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "No dialer app found", Toast.LENGTH_SHORT).show()
    }
}
@Composable
fun DriversScreenPreviewContent(
    drivers: List<DriverContact>
) {
    var searchQuery by remember { mutableStateOf("") }
    var sortMode by remember { mutableStateOf(DriverSortMode.NAME) }
    var selectedDriver by remember { mutableStateOf<DriverContact?>(null) }

    var showAddDialog by remember { mutableStateOf(false) }
    var editingDriver by remember { mutableStateOf<DriverContact?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    var localDrivers by remember { mutableStateOf(drivers) }

    val filteredAndSortedDrivers = remember(localDrivers, searchQuery, sortMode) {
        localDrivers
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

    // ADD dialog
    if (showAddDialog) {
        DriverEditDialog(
            title = "Add Driver",
            initialDriver = null,
            onDismiss = { showAddDialog = false },
            onSave = { newDriver ->
                localDrivers = localDrivers + newDriver
                showAddDialog = false
            }
        )
    }

    // EDIT dialog
    if (editingDriver != null) {
        DriverEditDialog(
            title = "Edit Driver",
            initialDriver = editingDriver,
            onDismiss = { editingDriver = null },
            onSave = { editedDriver ->
                val original = editingDriver ?: return@DriverEditDialog
                localDrivers = localDrivers.map { existing ->
                    if (existing == original) editedDriver else existing
                }

                if (selectedDriver == original) {
                    selectedDriver = editedDriver
                }

                editingDriver = null
            }
        )
    }

    // DELETE confirm
    if (showDeleteConfirm && selectedDriver != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Driver") },
            text = { Text("Delete ${selectedDriver!!.name}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val toDelete = selectedDriver ?: return@TextButton
                        localDrivers = localDrivers.filterNot { it == toDelete }
                        selectedDriver = null
                        showDeleteConfirm = false
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
                    onClick = { showDeleteConfirm = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = VtsTextPrimary_Light
                    )
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // DETAIL screen
    if (selectedDriver != null) {
        DriverDetailScreen(
            driver = selectedDriver!!,
            onBack = { selectedDriver = null },
            onCallDriver = {},
            onEditDriver = { editingDriver = selectedDriver },
            onDeleteDriver = { showDeleteConfirm = true }
        )
        return
    }

    // MAIN screen
    VtsScreenTemplate(
        title = "Drivers",
        showControls = true,
        dropdown = {
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
        searchBar = {
            DriverSearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it }
            )
        },
        sortBar = {
            DriverSortBar(
                sortMode = sortMode,
                onSortSelected = { sortMode = it }
            )
        }
    ) {
        DriversList(
            drivers = filteredAndSortedDrivers,
            onDriverClick = { driver ->
                selectedDriver = driver
            }
        )
    }
}

