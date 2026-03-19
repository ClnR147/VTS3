package com.example.vtsdaily3.ui.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.vtsdaily3.feature_lookup.domain.LookupTripDetail
import com.example.vtsdaily3.feature_lookup.ui.LookupLabelValueRow
import com.example.vtsdaily3.ui.components.VtsBackButton
import com.example.vtsdaily3.ui.components.VtsCard
import com.example.vtsdaily3.ui.components.directory.VtsDirectoryDetailCard
import com.example.vtsdaily3.ui.components.directory.VtsInfoRow
import com.example.vtsdaily3.ui.components.directory.VtsThinDivider
import com.example.vtsdaily3.ui.theme.VtsGreen
import com.example.vtsdaily3.ui.theme.VtsSpacing


@Preview(showBackground = true, widthDp = 412, heightDp = 915)
@Composable
private fun LookupDetailScreenWrapperPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = VtsSpacing.md, vertical = VtsSpacing.xl)
        ) {
            Text(
                text = "Passenger Lookup",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 20.dp),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )

            /* VtsBackButton(
                onClick = {},
                modifier = Modifier.padding(bottom = VtsSpacing.lg)
            ) */

            VtsDirectoryDetailCard(
                title = "Michael Tobin",
                showDivider = false
            ) {
                VtsInfoRow(
                    label = "Phone",
                    value = "619.315.5439"
                )
            }

            Spacer(modifier = Modifier.height(VtsSpacing.lg))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(VtsSpacing.lg)
            ) {
                LookupTripDateCard(
                    date = "7/4/2024",
                    trips = listOf(
                        LookupTripDetail(
                            pickup = "1058 Pellham Dr., Lompoc 93436",
                            dropoff = "127 W Pine Ave., Lompoc 93436"
                        )
                    )
                )

                LookupTripDateCard(
                    date = "7/9/2024",
                    trips = listOf(
                        LookupTripDetail(
                            pickup = "127 W Pine Ave., Lompoc 93436",
                            dropoff = "1058 Pellham Dr., Lompoc 93436"
                        ),
                        LookupTripDetail(
                            pickup = "1058 Pellham Dr., Lompoc 93436",
                            dropoff = "127 W Pine Ave., Lompoc 93436"
                        )
                    )
                )

                LookupTripDateCard(
                    date = "8/1/2024",
                    trips = listOf(
                        LookupTripDetail(
                            pickup = "127 W Pine Ave., Lompoc 93436",
                            dropoff = "1058 Pellham Dr., Lompoc 93436"
                        )
                    )
                )

                LookupTripDateCard(
                    date = "8/13/2024",
                    trips = listOf(
                        LookupTripDetail(
                            pickup = "1058 Pellham Dr., Lompoc 93436",
                            dropoff = "127 W Pine Ave., Lompoc 93436"
                        )
                    )
                )
            }
        }
    }
}

@Composable
private fun LookupTripDateCard(
    date: String,
    trips: List<LookupTripDetail>
) {
    VtsCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(VtsSpacing.xs)
        ) {
            Text(
                text = date,
                style = MaterialTheme.typography.titleMedium,
                color = VtsGreen,
                fontWeight = FontWeight.SemiBold
            )

            /* com.example.vtsdaily3.ui.template.VtsThinDivider() */

            trips.forEachIndexed { index, trip ->
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

                if (index < trips.lastIndex) {
                    Spacer(modifier = Modifier.height(VtsSpacing.sm))
                }
            }
        }
    }
}