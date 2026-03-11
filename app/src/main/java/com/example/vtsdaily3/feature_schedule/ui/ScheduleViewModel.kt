package com.example.vtsdaily3.feature_schedule.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vtsdaily3.feature_schedule.data.ScheduleRepository
import com.example.vtsdaily3.feature_schedule.ui.state.ScheduleUiState
import com.example.vtsdaily3.feature_schedule.ui.state.toUiState
import com.example.vtsdaily3.model.TripId
import com.example.vtsdaily3.model.TripStatus
import com.example.vtsdaily3.model.TripViewMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class ScheduleViewModel(
    private val repository: ScheduleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScheduleUiState(isLoading = true))
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    fun loadInitial() {
        viewModelScope.launch {
            try {
                val dates = repository.getAvailableDates()

                if (dates.isEmpty()) {
                    _uiState.value = ScheduleUiState(
                        isLoading = false,
                        errorMessage = "No schedule files found"
                    )
                    return@launch
                }

                val initialDate = if (LocalDate.now() in dates) {
                    LocalDate.now()
                } else {
                    dates.last()
                }

                loadDate(initialDate)
            } catch (e: Exception) {
                _uiState.value = ScheduleUiState(
                    isLoading = false,
                    errorMessage = e.message ?: "Unknown error"
                )
            }
        }
    }

    fun selectDate(date: LocalDate) {
        viewModelScope.launch {
            loadDate(date)
        }
    }

    fun refreshCurrentDate() {
        val date = _uiState.value.selectedDate ?: return
        selectDate(date)
    }

    fun selectViewMode(mode: TripViewMode) {
        val currentDate = _uiState.value.selectedDate ?: return
        viewModelScope.launch {
            val schedule = repository.loadSchedule(currentDate)
            _uiState.value = schedule.toUiState(mode)
        }
    }

    fun markTripStatus(tripId: TripId, status: TripStatus) {
        val date = _uiState.value.selectedDate ?: return

        viewModelScope.launch {
            repository.setTripStatus(date, tripId, status)
            loadDate(date)
        }
    }

    fun reinstateTrip(tripId: TripId) {
        val date = _uiState.value.selectedDate ?: return

        viewModelScope.launch {
            repository.clearTripStatus(date, tripId)
            loadDate(date)
        }
    }

    private suspend fun loadDate(date: LocalDate) {
        val viewMode = _uiState.value.selectedViewMode
        val schedule = repository.loadSchedule(date)
        _uiState.value = schedule.toUiState(viewMode)
    }
}