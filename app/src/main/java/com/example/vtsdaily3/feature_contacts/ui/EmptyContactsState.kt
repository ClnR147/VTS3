package com.example.vtsdaily3.feature_contacts.ui

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.vtsdaily3.feature_contacts.data.ContactEntry
import com.example.vtsdaily3.ui.components.VtsCard
import com.example.vtsdaily3.ui.components.VtsCardDensity
import com.example.vtsdaily3.ui.components.VtsSummaryRow
import com.example.vtsdaily3.ui.theme.VtsTextPrimary_Light

@Composable
fun EmptyContactsState(
    onAddContact: () -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "No contacts found.",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = "Add a contact for quick reference.",
            style = MaterialTheme.typography.bodyMedium
        )

        TextButton(
            onClick = onAddContact,
            colors = ButtonDefaults.textButtonColors(
                contentColor = VtsTextPrimary_Light
            )
        ) {
            Text("Add Contact")
        }
    }
}

@Composable
fun ContactsList(
    contacts: List<ContactEntry>,
    onContactClick: (ContactEntry) -> Unit,
    onContactLongClick: (ContactEntry) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(contacts) { contact ->
            ContactRowCard(
                contact = contact,
                onClick = { onContactClick(contact) },
                onLongClick = { onContactLongClick(contact) }
            )
        }
    }
}

@Composable
private fun ContactRowCard(
    contact: ContactEntry,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    VtsCard(
        modifier = Modifier.combinedClickable(
            onClick = onClick,
            onLongClick = onLongClick
        ),
        density = VtsCardDensity.Compact
    ) {
        VtsSummaryRow(
            title = contact.name,
            subtitle = contact.phone
        )
    }
}

@Composable
fun ContactEditDialog(
    title: String,
    initialContact: ContactEntry?,
    onDismiss: () -> Unit,
    onSave: (ContactEntry) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var name by remember(initialContact) { mutableStateOf(initialContact?.name.orEmpty()) }
    var phone by remember(initialContact) { mutableStateOf(initialContact?.phone.orEmpty()) }

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
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val trimmedName = name.trim()
                    val trimmedPhone = phone.trim()

                    if (trimmedName.isBlank()) return@TextButton

                    onSave(
                        ContactEntry(
                            name = trimmedName,
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
            androidx.compose.foundation.layout.Row {
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