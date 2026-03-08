package com.example.vtsdaily3.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.vtsdaily3.ui.theme.LightGreenCardBackground
import com.example.vtsdaily3.ui.theme.VtsGreen
import com.example.vtsdaily3.ui.theme.VtsShapes

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
