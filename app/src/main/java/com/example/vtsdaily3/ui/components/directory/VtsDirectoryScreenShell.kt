package com.example.vtsdaily3.ui.components.directory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.example.vtsdaily3.ui.components.VtsBackButton
import com.example.vtsdaily3.ui.components.VtsScreenHeader
import com.example.vtsdaily3.ui.theme.LightGreenCardBackground
import com.example.vtsdaily3.ui.theme.VtsGreen
import com.example.vtsdaily3.ui.theme.VtsShapes
import com.example.vtsdaily3.ui.theme.VtsSpacing

@Composable
fun VtsDirectoryScreenShell(
    title: String,
    showingDetail: Boolean = false,
    onBackFromDetail: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    searchValue: String? = null,
    onSearchValueChange: ((String) -> Unit)? = null,
    searchPlaceholder: String = "Search",
    sortBar: (@Composable () -> Unit)? = null,
    actionSlot: (@Composable () -> Unit)? = null,
    isListEmpty: Boolean,
    emptyState: @Composable () -> Unit = {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = VtsSpacing.xl),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No items found",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    },
    listContent: @Composable () -> Unit,
    detailContent: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = VtsSpacing.md)
    ) {
        VtsScreenHeader(
            title = title,
            showDivider = false
        )

        Spacer(modifier = Modifier.height(VtsSpacing.md))

        if (showingDetail && detailContent != null) {
            /* if (onBackFromDetail != null) {
                VtsBack(
                    onClick = onBackFromDetail,
                    modifier = Modifier.padding(bottom = VtsSpacing.sm)
                )
            } */

            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                detailContent()
            }
        } else {
            if (searchValue != null && onSearchValueChange != null) {
                VtsSearchField(
                    value = searchValue,
                    onValueChange = onSearchValueChange,
                    placeholder = searchPlaceholder
                )
                Spacer(modifier = Modifier.height(VtsSpacing.sm))
            }

            if (sortBar != null || actionSlot != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(VtsSpacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (sortBar != null) {
                        Box(
                            modifier = Modifier.weight(1f)
                        ) {
                            sortBar()
                        }
                    }

                    if (actionSlot != null) {
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            actionSlot()
                        }
                    }
                }

                Spacer(modifier = Modifier.height(VtsSpacing.md))
            }

            VtsThinDivider()

            Spacer(modifier = Modifier.height(VtsSpacing.md))

            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                if (isListEmpty) {
                    emptyState()
                } else {
                    listContent()
                }
            }
        }
    }
}

@Composable
fun VtsSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        shape = VtsShapes.cardTight,
        textStyle = MaterialTheme.typography.titleSmall,
        placeholder = {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.titleSmall
            )
        },
        leadingIcon = {
            androidx.compose.material3.Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "Search"
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = VtsGreen,
            unfocusedBorderColor = VtsGreen,
            focusedLeadingIconColor = VtsGreen,
            unfocusedLeadingIconColor = VtsGreen,
            cursorColor = VtsGreen,
            focusedContainerColor = LightGreenCardBackground,
            unfocusedContainerColor = LightGreenCardBackground
        )
    )
}
