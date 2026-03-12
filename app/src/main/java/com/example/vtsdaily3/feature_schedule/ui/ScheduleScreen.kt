package com.example.vtsdaily3.feature_schedule.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.vtsdaily3.feature_schedule.ui.state.ScheduleUiState
import com.example.vtsdaily3.model.Trip
import com.example.vtsdaily3.model.TripId
import com.example.vtsdaily3.model.TripStatus
import com.example.vtsdaily3.model.TripViewMode
import com.example.vtsdaily3.ui.theme.ActionGreen
import com.example.vtsdaily3.ui.theme.ActiveColor
import com.example.vtsdaily3.ui.theme.CardHighlight
import com.example.vtsdaily3.ui.theme.CompletedColor
import com.example.vtsdaily3.ui.theme.FromGrey
import com.example.vtsdaily3.ui.theme.RemovedColor
import com.example.vtsdaily3.ui.theme.VtsError
import com.example.vtsdaily3.ui.theme.VtsWarning
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.core.net.toUri


@Composable
fun ScheduleScreen(
    uiState: ScheduleUiState,
    onSelectDate: (LocalDate) -> Unit,
    onSelectViewMode: (TripViewMode) -> Unit,
    onMarkTripStatus: (TripId, TripStatus) -> Unit,
    onReinstateTrip: (TripId) -> Unit,
    onRefresh: () -> Unit,
    onPreviousDate: () -> Unit,
    onNextDate: () -> Unit
) {
    val formattedSelectedDate = remember(uiState.selectedDate) {
        uiState.selectedDate.format(
            DateTimeFormatter.ofPattern("EEE, MMM d, yyyy")
        )
    }
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        ScheduleHeaderCard(
            selectedDateText = formattedSelectedDate,
            selectedViewMode = uiState.selectedViewMode,
            activeCount = uiState.activeCount,
            completedCount = uiState.completedCount,
            otherCount = uiState.otherCount,
            onPreviousDate = onPreviousDate,
            onNextDate = onNextDate,
            onSelectViewMode = onSelectViewMode,
            modifier = Modifier.padding(top = 32.dp)
        )

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.tripsForSelectedView.isEmpty() -> {
                EmptyScheduleState(
                    viewMode = uiState.selectedViewMode,
                    modifier = Modifier.fillMaxSize()
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = uiState.tripsForSelectedView,
                        key = { trip -> trip.id.toString() }
                    ) { trip ->
                        TripCard(
                            trip = trip,
                            viewMode = uiState.selectedViewMode,
                            onTripActionSelected = { menuAction ->
                                when (menuAction) {
                                    TripMenuAction.COMPLETE -> {
                                        onMarkTripStatus(trip.id, TripStatus.COMPLETED)
                                    }
                                    TripMenuAction.NOSHOW -> {
                                        onMarkTripStatus(trip.id, TripStatus.NOSHOW)
                                    }
                                    TripMenuAction.CANCEL -> {
                                        onMarkTripStatus(trip.id, TripStatus.CANCELLED)
                                    }
                                    TripMenuAction.REMOVE -> {
                                        onMarkTripStatus(trip.id, TripStatus.REMOVED)
                                    }
                                    TripMenuAction.REINSTATE -> {
                                        onReinstateTrip(trip.id)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TripCard(
    trip: Trip,
    viewMode: TripViewMode,
    onTripActionSelected: (TripMenuAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    val toAddress = trip.toAddress.trim()
    val nameStartOffset = 150.dp

    val statusColor = when (viewMode) {
        TripViewMode.ACTIVE -> ActiveColor
        TripViewMode.COMPLETED -> CompletedColor
        TripViewMode.OTHER -> RemovedColor
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(nameStartOffset)
                        .clickable {
                            val phone = trip.phone.trim()
                            if (phone.isNotBlank()) {
                                context.startActivity(
                                    Intent(Intent.ACTION_DIAL, "tel:$phone".toUri())
                                )
                            } else {
                                Toast.makeText(
                                    context,
                                    "No phone number available",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                ) {
                    Text(
                        text = trip.time,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = CardHighlight.copy(alpha = 0.65f),
                        maxLines = 1,
                        overflow = TextOverflow.Clip
                    )
                }

                Text(
                    text = trip.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(10.dp))

            ClickableAlignedLineV3(
                label = "From:",
                value = trip.fromAddress,
                expanded = expanded,
                onClick = { launchWaze(context, trip.fromAddress) },
                onLongClick = { launchWaze(context, trip.fromAddress) }
            )

            if (toAddress.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                ClickableAlignedLineV3(
                    label = "To:",
                    value = toAddress,
                    expanded = expanded,
                    onClick = { launchWaze(context, toAddress) },
                    onLongClick = { launchWaze(context, toAddress) }
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    when (viewMode) {
                        TripViewMode.ACTIVE -> {
                            SmallPillV3("Complete") {
                                onTripActionSelected(TripMenuAction.COMPLETE)
                            }
                            SmallPillV3("No Show") {
                                onTripActionSelected(TripMenuAction.NOSHOW)
                            }
                            SmallPillV3("Cancel") {
                                onTripActionSelected(TripMenuAction.CANCEL)
                            }
                            SmallPillV3("Remove") {
                                onTripActionSelected(TripMenuAction.REMOVE)
                            }
                        }

                        TripViewMode.COMPLETED -> {
                            SmallPillV3("Reinstate") {
                                onTripActionSelected(TripMenuAction.REINSTATE)
                            }
                            Spacer(Modifier.width(6.dp))
                            OtherReasonBadgeV3("COMPLETED")
                        }

                        TripViewMode.OTHER -> {
                            SmallPillV3("Reinstate") {
                                onTripActionSelected(TripMenuAction.REINSTATE)
                            }

                            val reason = trip.status.otherLabelV3()
                            if (reason.isNotBlank()) {
                                Spacer(Modifier.width(6.dp))
                                OtherReasonBadgeV3(reason)
                            }
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (trip.phone.isNotBlank()) {
                        IconButton(
                            onClick = {
                                context.startActivity(
                                    Intent(Intent.ACTION_DIAL, "tel:${trip.phone.trim()}".toUri())
                                )
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Phone,
                                contentDescription = "Call Passenger",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            val navAddress = toAddress.ifBlank { trip.fromAddress }
                            if (navAddress.isNotBlank()) {
                                launchWaze(context, navAddress)
                            }
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = "Navigate",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyScheduleState(
    viewMode: TripViewMode,
    modifier: Modifier = Modifier
) {
    val message = when (viewMode) {
        TripViewMode.ACTIVE -> "No active trips for this date."
        TripViewMode.COMPLETED -> "No completed trips for this date."
        TripViewMode.OTHER -> "No other trips for this date."
    }

    Box(
        modifier = modifier.padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

private fun availableActionsFor(status: TripStatus): List<TripMenuAction> {
    return when (status) {
        TripStatus.ACTIVE -> listOf(
            TripMenuAction.COMPLETE,
            TripMenuAction.CANCEL,
            TripMenuAction.NOSHOW,
            TripMenuAction.REMOVE
        )

        TripStatus.COMPLETED -> listOf(
            TripMenuAction.REINSTATE,
            TripMenuAction.REMOVE
        )

        TripStatus.CANCELLED,
        TripStatus.NOSHOW,
        TripStatus.REMOVED -> listOf(
            TripMenuAction.REINSTATE
        )
    }
}

private fun statusLabel(status: TripStatus): String {
    return when (status) {
        TripStatus.ACTIVE -> "Active"
        TripStatus.COMPLETED -> "Completed"
        TripStatus.REMOVED -> "Removed"
        TripStatus.CANCELLED -> "Cancelled"
        TripStatus.NOSHOW -> "No Show"
    }
}



enum class TripMenuAction(val label: String) {
    COMPLETE("Complete"),
    CANCEL("Cancel"),
    NOSHOW("No Show"),
    REMOVE("Remove"),
    REINSTATE("Reinstate")
}

@Composable
fun ScheduleHeaderCard(
    selectedDateText: String,
    selectedViewMode: TripViewMode,
    activeCount: Int,
    completedCount: Int,
    otherCount: Int,
    onPreviousDate: () -> Unit,
    onNextDate: () -> Unit,
    onSelectViewMode: (TripViewMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFEAF4EA)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HeaderNavButton(
                    text = "Prev",
                    onClick = onPreviousDate
                )

                Text(
                    text = selectedDateText,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1F2A1F)
                )

                HeaderNavButton(
                    text = "Next",
                    onClick = onNextDate
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ViewModeButton(
                    label = "Active",
                    selected = selectedViewMode == TripViewMode.ACTIVE,
                    onClick = { onSelectViewMode(TripViewMode.ACTIVE) },
                    modifier = Modifier.weight(1f)
                )

                ViewModeButton(
                    label = "Completed",
                    selected = selectedViewMode == TripViewMode.COMPLETED,
                    onClick = { onSelectViewMode(TripViewMode.COMPLETED) },
                    modifier = Modifier.weight(1f)
                )

                ViewModeButton(
                    label = "Other",
                    selected = selectedViewMode == TripViewMode.OTHER,
                    onClick = { onSelectViewMode(TripViewMode.OTHER) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                HeaderCountItem(
                    count = activeCount,
                    accentColor = Color(0xFF2E7D32),
                    modifier = Modifier.weight(1f)
                )

                HeaderCountItem(
                    count = completedCount,
                    accentColor = Color(0xFF1565C0),
                    modifier = Modifier.weight(1f)
                )

                HeaderCountItem(
                    count = otherCount,
                    accentColor = Color(0xFFEF6C00),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun HeaderNavButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .width(96.dp)
            .height(44.dp),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.5.dp, Color(0xFFB8C8B8)),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = Color(0xFF3B3B3B)
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ViewModeButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedFill = Color(0xFF4CAF50)
    val selectedText = Color.White
    val unselectedBorder = Color(0xFF81C784)
    val unselectedText = Color(0xFF4CAF50)

    if (selected) {
        Button(
            onClick = onClick,
            modifier = modifier.height(44.dp),
            shape = RoundedCornerShape(24.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = selectedFill,
                contentColor = selectedText
            )
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier.height(44.dp),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.5.dp, unselectedBorder),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.Transparent,
                contentColor = unselectedText
            )
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun HeaderCountItem(
    count: Int,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = accentColor
        )
    }
}

@Composable
private fun ClickableAlignedLineV3(
    label: String,
    value: String,
    expanded: Boolean,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    if (value.isBlank()) return

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = FromGrey,
            modifier = Modifier.width(56.dp)
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = if (expanded) 3 else 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                )
        )
    }
}

@Composable
private fun SmallPillV3(
    text: String,
    onClick: () -> Unit
) {
    val base = when (text) {
        "Complete" -> ActionGreen
        "No Show" -> VtsWarning
        "Cancel" -> VtsError
        "Remove" -> RemovedColor
        "Reinstate" -> ActionGreen
        else -> MaterialTheme.colorScheme.primary
    }

    val bg = base.copy(alpha = 0.14f)
    val border = base.copy(alpha = 0.45f)
    val fg = MaterialTheme.colorScheme.onSurface

    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.height(28.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
        border = BorderStroke(1.dp, border),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = bg,
            contentColor = fg
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1
        )
    }
}

@Composable
private fun OtherReasonBadgeV3(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = VtsError
    )
}

private fun launchWaze(context: Context, address: String) {
    val q = Uri.encode(address.trim())
    if (q.isBlank()) return

    fun start(intent: Intent): Boolean {
        // if we ever get an application context, make it safe
        if (context !is Activity) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        return try {
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }

    // 1) Try Waze native scheme (some builds support it)
    if (start(Intent(Intent.ACTION_VIEW, "waze://?q=$q".toUri()))) return

    // 2) Try https deep link WITHOUT forcing package (lets Android pick Waze)
    if (start(Intent(Intent.ACTION_VIEW, "https://waze.com/ul?q=$q".toUri()))) return

    // 3) Last resort: geo: (often still offers Waze)
    start(Intent(Intent.ACTION_VIEW, "geo:0,0?q=$q".toUri()))
}
private fun TripStatus.otherLabelV3(): String = when (this) {
    TripStatus.CANCELLED -> "CANCEL"
    TripStatus.NOSHOW -> "NO SHOW"
    TripStatus.REMOVED -> "REMOVED"
    else -> ""
}
