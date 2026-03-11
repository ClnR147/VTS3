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

    private val _uiState = MutableStateFlow(
        ScheduleUiState(isLoading = true)
    )
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    fun loadInitial() {
        viewModelScope.launch {
            try {
                val dates = repository.getAvailableDates()

                if (dates.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "No schedule files found",
                        tripsForSelectedView = emptyList(),
                        activeCount = 0,
                        completedCount = 0,
                        otherCount = 0
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
                _uiState.value = _uiState.value.copy(
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
        selectDate(_uiState.value.selectedDate)
    }

    fun selectViewMode(mode: TripViewMode) {
        viewModelScope.launch {
            val currentDate = _uiState.value.selectedDate
            val schedule = repository.loadSchedule(currentDate)
            _uiState.value = schedule.toUiState(mode)
        }
    }

    fun markTripStatus(tripId: TripId, status: TripStatus) {
        viewModelScope.launch {
            val date = _uiState.value.selectedDate
            repository.setTripStatus(date, tripId, status)
            loadDate(date)
        }
    }

    fun reinstateTrip(tripId: TripId) {
        viewModelScope.launch {
            val date = _uiState.value.selectedDate
            repository.clearTripStatus(date, tripId)
            loadDate(date)
        }
    }

    private suspend fun loadDate(date: LocalDate) {
        val viewMode = _uiState.value.selectedViewMode

        _uiState.value = _uiState.value.copy(
            isLoading = true,
            errorMessage = null
        )

        try {
            val schedule = repository.loadSchedule(date)
            _uiState.value = schedule.toUiState(viewMode)
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                selectedDate = date,
                isLoading = false,
                errorMessage = e.message ?: "Unknown error"
            )
        }
    }
}