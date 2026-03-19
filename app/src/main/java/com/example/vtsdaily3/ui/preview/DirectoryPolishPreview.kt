package com.example.vtsdaily3.ui.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.vtsdaily3.feature_drivers.data.DriverContact
import com.example.vtsdaily3.feature_lookup.domain.LookupTripDetail
import com.example.vtsdaily3.ui.components.VtsCard
import com.example.vtsdaily3.ui.components.VtsCardDensity
import com.example.vtsdaily3.ui.components.VtsSummaryRow
import com.example.vtsdaily3.ui.components.directory.VtsDirectoryDetailCard
import com.example.vtsdaily3.ui.components.directory.VtsInfoRow
import com.example.vtsdaily3.ui.components.directory.VtsThinDivider
import com.example.vtsdaily3.ui.theme.VtsGreen
import com.example.vtsdaily3.ui.theme.VtsSpacing
import org.apache.poi.ss.formula.functions.Column


@Preview(showBackground = true, widthDp = 420, heightDp = 1100)
@Composable
private fun DirectoryPolishPreview_AllScenarios() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(VtsSpacing.md),
            verticalArrangement = Arrangement.spacedBy(VtsSpacing.lg)
        ) {
            Text(
                text = "Scenario 1: Lookup Header",
                style = MaterialTheme.typography.titleMedium,
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

            Text(
                text = "Scenario 2: Lookup Trip Date Card",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            LookupTripDateCardPreviewContent()

            Text(
                text = "Scenario 3: Driver Row",
                style = MaterialTheme.typography.titleMedium,
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
}

@Composable
private fun LookupTripDateCardPreviewContent() {
    VtsCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(VtsSpacing.md)
        ) {
            Text(
                text = "03/18/2026",
                style = MaterialTheme.typography.titleMedium,
                color = VtsGreen,
                fontWeight = FontWeight.SemiBold
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(VtsSpacing.sm)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(VtsSpacing.xs)
                ) {
                    LookupLabelValueRow(
                        label = "Pickup:",
                        value = "123 Main Street, Santa Maria, CA"
                    )
                    LookupLabelValueRow(
                        label = "Drop-off:",
                        value = "Marian Regional Medical Center"
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(VtsSpacing.xs)
                ) {
                    LookupLabelValueRow(
                        label = "Pickup:",
                        value = "456 Oak Avenue, Santa Maria, CA"
                    )
                    LookupLabelValueRow(
                        label = "Drop-off:",
                        value = "Lompoc Dialysis Center"
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 420)
@Composable
private fun LookupHeaderOnlyPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(VtsSpacing.md)
        ) {
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
}

@Preview(showBackground = true, widthDp = 420)
@Composable
private fun LookupTripDateCardOnlyPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(VtsSpacing.md)
        ) {
            LookupTripDateCardPreviewContent()
        }
    }
}

@Preview(showBackground = true, widthDp = 420)
@Composable
private fun DriverRowOnlyPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(VtsSpacing.md)
        ) {
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
}
@Preview(showBackground = true, widthDp = 1000, heightDp = 900)
@Composable
private fun DirectoryPolishPreview_BeforeAfter() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(VtsSpacing.md),
            verticalArrangement = Arrangement.spacedBy(VtsSpacing.lg)
        ) {
            Text(
                text = "Before / After Comparison",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "Lookup Header",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(VtsSpacing.lg)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Before",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(VtsSpacing.sm))
                    LookupHeaderBeforePreviewContent()
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "After",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(VtsSpacing.sm))
                    LookupHeaderAfterPreviewContent()
                }
            }

            Text(
                text = "Lookup Trip Date Card",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(VtsSpacing.lg)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Before",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(VtsSpacing.sm))
                    LookupTripDateCardBeforePreviewContent()
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "After",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(VtsSpacing.sm))
                    LookupTripDateCardAfterPreviewContent()
                }
            }
        }
    }
}

@Composable
private fun LookupHeaderBeforePreviewContent() {
    VtsCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(VtsSpacing.sm)
        ) {
            Text(
                text = "Jane Doe",
                style = MaterialTheme.typography.headlineSmall
            )

            VtsThinDivider()

            VtsInfoRow(
                label = "Phone",
                value = "(805) 555-1234"
            )
        }
    }
}

@Composable
private fun LookupHeaderAfterPreviewContent() {
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

@Composable
private fun LookupTripDateCardBeforePreviewContent() {
    VtsCard {
        Text(
            text = "03/18/2026",
            style = MaterialTheme.typography.titleMedium,
            color = VtsGreen,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(6.dp))
        Spacer(Modifier.height(VtsSpacing.sm))

        listOf(
            LookupTripDetail(
                pickup = "123 Main Street, Santa Maria, CA",
                dropoff = "Marian Regional Medical Center"
            ),
            LookupTripDetail(
                pickup = "456 Oak Avenue, Santa Maria, CA",
                dropoff = "Lompoc Dialysis Center"
            )
        ).forEachIndexed { index, trip ->
            if (index > 0) {
                Spacer(Modifier.height(VtsSpacing.sm))
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(VtsSpacing.xs)
            ) {
                LookupLabelValueRow(
                    label = "Pickup:",
                    value = trip.pickup
                )
                LookupLabelValueRow(
                    label = "Drop-off:",
                    value = trip.dropoff
                )
            }
        }
    }
}

@Composable
private fun LookupTripDateCardAfterPreviewContent() {
    VtsCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(VtsSpacing.md)
        ) {
            Text(
                text = "03/18/2026",
                style = MaterialTheme.typography.titleMedium,
                color = VtsGreen,
                fontWeight = FontWeight.SemiBold
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(VtsSpacing.sm)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(VtsSpacing.xs)
                ) {
                    LookupLabelValueRow(
                        label = "Pickup:",
                        value = "123 Main Street, Santa Maria, CA"
                    )
                    LookupLabelValueRow(
                        label = "Drop-off:",
                        value = "Marian Regional Medical Center"
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(VtsSpacing.xs)
                ) {
                    LookupLabelValueRow(
                        label = "Pickup:",
                        value = "456 Oak Avenue, Santa Maria, CA"
                    )
                    LookupLabelValueRow(
                        label = "Drop-off:",
                        value = "Lompoc Dialysis Center"
                    )
                }
            }
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
private fun LookupLabelValueRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(VtsSpacing.sm),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            modifier = Modifier.width(72.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = value,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}