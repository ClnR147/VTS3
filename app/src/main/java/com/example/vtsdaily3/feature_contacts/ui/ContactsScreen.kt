package com.example.vtsdaily3.feature_contacts.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
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
import com.example.vtsdaily3.feature_contacts.data.ContactEntry
import com.example.vtsdaily3.feature_contacts.data.ContactStore
import com.example.vtsdaily3.ui.components.directory.VtsDirectoryScreenShell
import com.example.vtsdaily3.ui.components.VtsOverflowMenu
import com.example.vtsdaily3.ui.theme.VtsSpacing
import com.example.vtsdaily3.ui.theme.VtsTextPrimary_Light
import java.io.BufferedReader
import java.io.InputStreamReader
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.util.Log
import androidx.compose.material3.DropdownMenuItem
import androidx.core.net.toUri


enum class ContactSortMode {
    NAME
}

@Composable
fun ContactsScreen() {
    val context = LocalContext.current

    var contacts by remember { mutableStateOf<List<ContactEntry>>(emptyList()) }

    fun reloadContacts() {
        contacts = ContactStore.load(context)
    }

    val importContactsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) {
            Log.d("ContactsImport", "Import cancelled")
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
            val imported = parseContactsCsv(context, uri)
            ContactStore.save(context, imported)
            reloadContacts()
            Log.d("ContactsImport", "Imported ${imported.size} contacts")
        } catch (e: Exception) {
            Log.e("ContactsImport", "Import failed", e)
        }
    }

    LaunchedEffect(Unit) {
        reloadContacts()
    }

    ContactsScreenContent(
        contacts = contacts,
        onContactsChange = { updated ->
            contacts = updated
            ContactStore.save(context, updated)
        },
        onCallContact = { contact ->
            val cleanedPhone = contact.phone.filter { it.isDigit() || it == '+' }
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = "tel:$cleanedPhone".toUri()
            }
            context.startActivity(intent)
        },
        onImportContacts = {
            importContactsLauncher.launch(arrayOf("*/*"))
        }
    )
}

@Composable
fun ContactsScreenContent(
    contacts: List<ContactEntry>,
    onContactsChange: (List<ContactEntry>) -> Unit,
    onCallContact: (ContactEntry) -> Unit,
    onImportContacts: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var sortMode by remember { mutableStateOf(ContactSortMode.NAME) }
    var menuExpanded by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingContact by remember { mutableStateOf<ContactEntry?>(null) }
    var contactPendingDelete by remember { mutableStateOf<ContactEntry?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val filteredAndSortedContacts = remember(contacts, searchQuery, sortMode) {
        contacts
            .filter { contact ->
                val query = searchQuery.trim()
                if (query.isBlank()) {
                    true
                } else {
                    contact.name.contains(query, ignoreCase = true)
                }
            }
            .sortedWith(
                when (sortMode) {
                    ContactSortMode.NAME ->
                        compareBy(String.CASE_INSENSITIVE_ORDER) { it.name }
                }
            )
    }

    if (showAddDialog) {
        ContactEditDialog(
            title = "Add Contact",
            initialContact = null,
            onDismiss = { showAddDialog = false },
            onSave = { newContact ->
                onContactsChange(contacts + newContact)
                showAddDialog = false
            }
        )
    }

    if (editingContact != null) {
        ContactEditDialog(
            title = "Edit Contact",
            initialContact = editingContact,
            onDismiss = { editingContact = null },
            onDelete = {
                contactPendingDelete = editingContact
                showDeleteConfirm = true
                editingContact = null
            },
            onSave = { editedContact ->
                val original = editingContact ?: return@ContactEditDialog
                val updated = contacts.map { existing ->
                    if (existing == original) editedContact else existing
                }
                onContactsChange(updated)
                editingContact = null
            }
        )
    }

    if (showDeleteConfirm && contactPendingDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirm = false
                contactPendingDelete = null
            },
            title = { Text("Delete Contact") },
            text = {
                Text("Delete ${contactPendingDelete!!.name}?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val toDelete = contactPendingDelete ?: return@TextButton
                        val updated = contacts.filterNot { it == toDelete }
                        onContactsChange(updated)
                        showDeleteConfirm = false
                        contactPendingDelete = null
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
                        contactPendingDelete = null
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
        title = "Contacts",
        showingDetail = false,
        searchValue = searchQuery,
        onSearchValueChange = { searchQuery = it },
        searchPlaceholder = "Search contacts",
        sortOptions = listOf("Name"),
        selectedSortOption = when (sortMode) {
            ContactSortMode.NAME -> "Name"
        },
        onSortOptionSelected = { option ->
            sortMode = when (option) {
                "Name" -> ContactSortMode.NAME
                else -> sortMode
            }
        },
        actionSlot = {
            VtsOverflowMenu(
                expanded = menuExpanded,
                onExpandedChange = { menuExpanded = it }
            ) {
                ContactMenuContent(
                    onAddContact = {
                        menuExpanded = false
                        showAddDialog = true
                    },
                    onImportContacts = {
                        menuExpanded = false
                        onImportContacts()
                    }
                )
            }
        },
        isListEmpty = filteredAndSortedContacts.isEmpty(),
        emptyState = {
            if (contacts.isEmpty()) {
                EmptyContactsState(
                    onAddContact = { showAddDialog = true }
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = VtsSpacing.xl),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No contacts found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        listContent = {
            ContactsList(
                contacts = filteredAndSortedContacts,
                onContactClick = { contact ->
                    onCallContact(contact)
                },
                onContactLongClick = { contact ->
                    editingContact = contact
                }
            )
        }
    )
}

private fun parseContactsCsv(
    context: android.content.Context,
    uri: Uri
): List<ContactEntry> {
    val rows = mutableListOf<ContactEntry>()

    context.contentResolver.openInputStream(uri)?.use { inputStream ->
        BufferedReader(InputStreamReader(inputStream)).use { reader ->
            reader.lineSequence()
                .drop(1) // skip header
                .forEach { line ->
                    val trimmed = line.trim()
                    if (trimmed.isBlank()) return@forEach

                    val parts = trimmed.split(",")
                    if (parts.size < 2) return@forEach

                    val name = parts[0].trim()
                    val phone = parts[1].trim()

                    if (name.isNotBlank()) {
                        rows.add(
                            ContactEntry(
                                name = name,
                                phone = phone
                            )
                        )
                    }
                }
        }
    }

    return rows
}
@Composable
private fun ContactMenuContent(
    onAddContact: () -> Unit,
    onImportContacts: () -> Unit
) {
    DropdownMenuItem(
        text = { Text("Add Contact") },
        onClick = onAddContact
    )

    DropdownMenuItem(
        text = { Text("Import Contacts") },
        onClick = onImportContacts
    )
}