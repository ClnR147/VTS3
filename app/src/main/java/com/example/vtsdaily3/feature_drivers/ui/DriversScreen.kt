package com.example.vtsdaily3.feature_drivers.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import com.example.vtsdaily3.feature_drivers.data.DriverContact
import com.example.vtsdaily3.feature_drivers.data.DriverStore
import com.example.vtsdaily3.feature_drivers.data.DriversFolderPrefs
import com.example.vtsdaily3.ui.template.VtsScreenTemplate
import android.content.ActivityNotFoundException
import android.content.Context
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import com.example.vtsdaily3.ui.theme.VtsTextPrimary_Light

@Composable
fun DriversScreen() {
    val context = LocalContext.current

    var drivers by remember { mutableStateOf<List<DriverContact>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var sortMode by remember { mutableStateOf(DriverSortMode.NAME) }
    var selectedDriver by remember { mutableStateOf<DriverContact?>(null) }

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

    if (selectedDriver != null) {
        DriverDetailScreen(
            driver = selectedDriver!!,
            onBack = { selectedDriver = null },
            onCallDriver = { phone ->
                launchDialer(context, phone)
            }
        )
        return
    }

    VtsScreenTemplate(
        title = "Drivers",
        showControls = true,
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
                onChooseFolder = { folderPickerLauncher.launch(null) }
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
    onChooseFolder: () -> Unit
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
            text = "Choose the folder that contains drivers.json.",
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            text = "Tap here to choose folder",
            style = MaterialTheme.typography.bodyLarge,
            color = VtsTextPrimary_Light,
            modifier = Modifier.clickable { onChooseFolder() }
        )
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = driver.name,
                style = MaterialTheme.typography.titleMedium
            )

            ThinDriverDivider()

            DriverLabelValueRow(
                label = "Phone",
                value = driver.phone
            )

            DriverLabelValueRow(
                label = "Van",
                value = driver.vanNumber
            )
        }
    }
}

@Composable
private fun DriverDetailScreen(
    driver: DriverContact,
    onBack: () -> Unit,
    onCallDriver: (String) -> Unit
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
                onClick = onBack
            ) {
                Text(
                    text = "Back",
                    color = VtsTextPrimary_Light
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = driver.name,
                            style = MaterialTheme.typography.headlineSmall
                        )

                        IconButton(
                            onClick = { onCallDriver(driver.phone) }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Call,
                                contentDescription = "Call driver",
                                tint = VtsTextPrimary_Light
                            )
                        }
                    }

                    ThinDriverDivider()

                    DriverLabelValueRow(
                        label = "Phone",
                        value = driver.phone
                    )

                    DriverLabelValueRow(
                        label = "Van",
                        value = driver.vanNumber
                    )
                }
            }
        }
    }
}

@Composable
private fun DriverLabelValueRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private fun launchDialer(context: Context, phone: String) {
    val cleaned = phone.trim()

    if (cleaned.isBlank()) {
        Toast.makeText(context, "No phone number available", Toast.LENGTH_SHORT).show()
        return
    }

    val intent = Intent(Intent.ACTION_DIAL).apply {
        data = Uri.parse("tel:$cleaned")
    }

    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "No dialer app found", Toast.LENGTH_SHORT).show()
    }
}
@Composable
private fun ThinDriverDivider() {
    HorizontalDivider(
        modifier = Modifier.fillMaxWidth(),
        thickness = 1.dp
    )
}