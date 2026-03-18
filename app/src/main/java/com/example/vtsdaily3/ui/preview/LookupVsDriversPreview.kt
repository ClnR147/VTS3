package com.example.vtsdaily3.ui.preview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.vtsdaily3.feature_drivers.data.DriverContact
import com.example.vtsdaily3.feature_drivers.ui.DriversScreenPreviewContent
import com.example.vtsdaily3.feature_lookup.ui.LookupScreen
import com.example.vtsdaily3.ui.theme.Vts3DailyTheme

@Preview(
    showBackground = true,
    widthDp = 800,
    heightDp = 900
)
@Composable
fun LookupVsDriversPreview() {
    Vts3DailyTheme {
        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                LookupScreen()
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                DriversScreenPreview()
            }
        }
    }
}

@Composable
private fun DriversScreenPreview() {
    val previewDrivers = listOf(
        DriverContact(
            name = "John Smith",
            phone = "805-555-1111",
            vanNumber = "Van 12"
        ),
        DriverContact(
            name = "Maria Lopez",
            phone = "805-555-2222",
            vanNumber = "Van 7"
        ),
        DriverContact(
            name = "David Chen",
            phone = "805-555-3333",
            vanNumber = "Van 3"
        )
    )

    DriversScreenPreviewContent(
        drivers = previewDrivers
    )
}