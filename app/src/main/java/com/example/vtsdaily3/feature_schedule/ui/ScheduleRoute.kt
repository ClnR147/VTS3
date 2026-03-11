package com.example.vtsdaily3.feature_schedule.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.vtsdaily3.model.TripStatus
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ScheduleRoute(
    viewModel: ScheduleViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ScheduleScreen(
        state = uiState,
        onAction = { action ->
            when (action) {
                ScheduleUiAction.PreviousDayClicked -> {
                    viewModel.selectDate(uiState.selectedDate.minusDays(1))
                }

                ScheduleUiAction.NextDayClicked -> {
                    viewModel.selectDate(uiState.selectedDate.plusDays(1))
                }

                is ScheduleUiAction.ViewModeSelected -> {
                    viewModel.selectViewMode(action.mode)
                }

                is ScheduleUiAction.TripActionSelected -> {
                    when (action.action) {
                        TripMenuAction.COMPLETE -> {
                            viewModel.markTripStatus(action.tripId, TripStatus.COMPLETED)
                        }

                        TripMenuAction.CANCEL -> {
                            viewModel.markTripStatus(action.tripId, TripStatus.CANCELLED)
                        }

                        TripMenuAction.NOSHOW -> {
                            viewModel.markTripStatus(action.tripId, TripStatus.NOSHOW)
                        }

                        TripMenuAction.REMOVE -> {
                            viewModel.markTripStatus(action.tripId, TripStatus.REMOVED)
                        }

                        TripMenuAction.REINSTATE -> {
                            viewModel.reinstateTrip(action.tripId)
                        }
                    }
                }
            }
        }
    )
}