package com.example.vtsdaily3.ui.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.vtsdaily3.feature_lookup.ui.LookupScreen
import com.example.vtsdaily3.feature_schedule.ui.ScheduleHeaderCard
import com.example.vtsdaily3.feature_schedule.ui.ViewModeButton
import com.example.vtsdaily3.model.TripViewMode

@Preview(
    name = "Schedule Screen",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=411dp,height=891dp"
)
@Composable
private fun ScheduleScreenPolishPreview() {
    MaterialTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            SchedulePreviewContent()
        }
    }
}


@Preview(name = "Lookup Screen", showBackground = true, showSystemUi = true)
@Composable
private fun LookupScreenPolishPreview() {
    MaterialTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            LookupPreviewContent()
        }
    }
}


/*
@Preview(name = "Schedule vs Lookup", showBackground = true, widthDp = 1000)
@Composable
private fun ScheduleLookupComparisonPreview() {
    MaterialTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    SchedulePreviewContent()
                }

                Box(modifier = Modifier.weight(1f)) {
                    LookupPreviewContent()
                }
            }
        }
    }
}
*/
/*
@Preview(name = "Header Only", showBackground = true, widthDp = 420)
@Composable
private fun ScheduleHeaderOnlyPreview() {
    MaterialTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(44.dp)
            ) {
                TextHeader(title = "Schedule")

                Spacer(modifier = Modifier.height(12.dp))

                ScheduleHeaderCard(
                    selectedDateText = "Tue, Mar 10, 2026",
                    selectedViewMode = TripViewMode.ACTIVE,
                    activeCount = 12,
                    completedCount = 4,
                    otherCount = 2,
                    onPreviousDate = {},
                    onNextDate = {},
                    onSelectViewMode = {}
                )
            }
        }
    }
} */

/*
@Preview(name = "ViewMode Buttons", showBackground = true, widthDp = 420)
@Composable
private fun ViewModeButtonLabPreview() {
    MaterialTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ViewModeButton(
                    label = "Active",
                    selected = true,
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )

                ViewModeButton(
                    label = "Completed",
                    selected = false,
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )

                ViewModeButton(
                    label = "Other",
                    selected = false,
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
*/
@Composable
private fun SchedulePreviewContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp)
    ) {
        TextHeader(title = "Schedule")

        Spacer(modifier = Modifier.height(20.dp))

        ScheduleHeaderCard(
            selectedDateText = "Tue, Mar 10, 2026",
            selectedViewMode = TripViewMode.ACTIVE,
            activeCount = 12,
            completedCount = 4,
            otherCount = 2,
            onPreviousDate = {},
            onNextDate = {},
            onSelectViewMode = {}
        )
    }
}

@Composable
private fun LookupPreviewContent() {
    LookupScreen()
}

@Composable
private fun TextHeader(title: String) {
    androidx.compose.material3.Text(
        text = title,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
    )
}
/*
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ScheduleScreenPreview() {

    MaterialTheme {

        Surface(color = MaterialTheme.colorScheme.background) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {

                Text(
                    text = "Schedule",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(Modifier.height(12.dp))

                ScheduleHeaderCard(
                    selectedDateText = "Tue, Mar 10, 2026",
                    selectedViewMode = TripViewMode.ACTIVE,
                    activeCount = 12,
                    completedCount = 4,
                    otherCount = 2,
                    onPreviousDate = {},
                    onNextDate = {},
                    onSelectViewMode = {}
                )

                Spacer(Modifier.height(8.dp))

                HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

                Spacer(Modifier.height(400.dp)) // fake list space
            }
        }
    }
}*/