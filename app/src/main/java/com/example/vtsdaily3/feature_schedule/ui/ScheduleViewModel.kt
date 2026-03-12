package com.example.vtsdaily3.feature_schedule.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vtsdaily3.feature_schedule.data.ScheduleRepository
import com.example.vtsdaily3.feature_schedule.domain.DailySchedule
import com.example.vtsdaily3.feature_schedule.ui.state.ScheduleUiMapper
import com.example.vtsdaily3.feature_schedule.ui.state.ScheduleUiState
import com.example.vtsdaily3.model.TripStatus
import com.example.vtsdaily3.model.TripViewMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import com.example.vtsdaily3.model.TripId

class ScheduleViewModel(
    private val repository: ScheduleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScheduleUiState(isLoading = true))
    val uiState: StateFlow<ScheduleUiState> = _uiState

    private var currentDailySchedule: DailySchedule? = null
    private var selectedViewMode: TripViewMode = TripViewMode.ACTIVE

    init {
        loadDate(LocalDate.now())
    }

    fun selectDate(date: LocalDate) {
        loadDate(date)
    }

    fun selectViewMode(viewMode: TripViewMode) {
        selectedViewMode = viewMode
        val schedule = currentDailySchedule ?: return

        _uiState.value = ScheduleUiMapper.map(
            dailySchedule = schedule,
            selectedViewMode = selectedViewMode,
            isLoading = false,
            errorMessage = null
        )
    }

    private fun navigateBy(offset: Int) {
        val dates = _uiState.value.availableDates
        val currentDate = _uiState.value.selectedDate

        if (dates.isEmpty()) return

        val currentIndex = dates.indexOf(currentDate)
        if (currentIndex == -1) return

        val targetIndex = currentIndex + offset
        if (targetIndex !in dates.indices) return

        loadDate(dates[targetIndex])
    }

    fun goToPreviousAvailableDate() = navigateBy(-1)

    fun goToNextAvailableDate() = navigateBy(1)

    fun refreshCurrentDate() {
        val date = currentDailySchedule?.date ?: _uiState.value.selectedDate
        loadDate(date)
    }

    fun markTripStatus(tripId: TripId, status: TripStatus) {
        viewModelScope.launch {
            try {
                repository.setTripStatus(
                    date = _uiState.value.selectedDate,
                    tripId = tripId,
                    status = status
                )
                refreshCurrentDate()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Failed to update trip status."
                )
            }
        }
    }

    fun reinstateTrip(tripId: TripId) {
        viewModelScope.launch {
            try {
                repository.clearTripStatus(
                    date = _uiState.value.selectedDate,
                    tripId = tripId
                )
                refreshCurrentDate()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Failed to reinstate trip."
                )
            }
        }
    }

    private fun loadDate(date: LocalDate) {
        _uiState.value = _uiState.value.copy(
            selectedDate = date,
            isLoading = true,
            errorMessage = null
        )

        viewModelScope.launch {
            try {
                val dailySchedule = repository.loadSchedule(date)
                currentDailySchedule = dailySchedule

                _uiState.value = ScheduleUiMapper.map(
                    dailySchedule = dailySchedule,
                    selectedViewMode = selectedViewMode,
                    isLoading = false,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    selectedDate = date,
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load schedule."
                )
            }
        }
    }
}
