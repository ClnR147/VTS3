package com.example.vtsdaily3.ui.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.example.vtsdaily3.feature_drivers.data.DriverContact
import com.example.vtsdaily3.feature_drivers.ui.DriverRowCard
import com.example.vtsdaily3.ui.components.VtsCard
import com.example.vtsdaily3.ui.components.VtsCardDensity
import com.example.vtsdaily3.ui.components.directory.VtsDirectoryDetailCard
import com.example.vtsdaily3.ui.components.directory.VtsInfoRow
import com.example.vtsdaily3.ui.components.directory.VtsThinDivider
import com.example.vtsdaily3.ui.theme.VtsSpacing

@Preview(showBackground = true, widthDp = 1000, heightDp = 700)
@Composable
private fun WrapperPreview_LookupHeader_vs_DriverHeader() {
    MaterialTheme {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(VtsSpacing.md),
            horizontalArrangement = Arrangement.spacedBy(VtsSpacing.lg)
        ) {
            PreviewPane(
                modifier = Modifier.weight(1f),
                title = "Lookup Header"
            ) {
                LookupHeaderWrapperPreview()
            }

            PreviewPane(
                modifier = Modifier.weight(1f),
                title = "Driver Header"
            ) {
                DriverHeaderWrapperPreview()
            }
        }
    }
}

@Composable
private fun PreviewPane(
    modifier: Modifier = Modifier,
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    VtsCard(
        modifier = modifier,
        density = VtsCardDensity.Default
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(VtsSpacing.md)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            VtsThinDivider()

            content()
        }
    }
}

@Composable
private fun LookupHeaderWrapperPreview() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(VtsSpacing.md)
    ) {
        Text(
            text = "Passenger Lookup",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )

        VtsDirectoryDetailCard(
            title = "Jane Doe",
            showDivider = false
        ) {
            VtsInfoRow(
                label = "Phone",
                value = "(805) 555-1234"
            )
        }
    }
}

@Composable
private fun DriverHeaderWrapperPreview() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(VtsSpacing.md)
    ) {
        Text(
            text = "Drivers",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )

        DriverRowCard(
            driver = DriverContact(
                name = "John Smith",
                phone = "(805) 555-9876",
                vanNumber = "Van 12"
            ),
            onClick = {}
        )
    }
}
