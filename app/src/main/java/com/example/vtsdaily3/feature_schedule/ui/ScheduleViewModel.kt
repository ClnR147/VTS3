package com.example.vtsdaily3.feature_schedule.ui

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vtsdaily3.feature_schedule.data.ScheduleRepository
import com.example.vtsdaily3.feature_schedule.domain.DailySchedule
import com.example.vtsdaily3.feature_schedule.ui.state.ScheduleUiMapper
import com.example.vtsdaily3.feature_schedule.ui.state.ScheduleUiState
import com.example.vtsdaily3.model.Trip
import com.example.vtsdaily3.model.TripStatus
import com.example.vtsdaily3.model.TripViewMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import com.example.vtsdaily3.model.TripId
import kotlinx.coroutines.flow.asStateFlow
import com.example.vtsdaily3.feature_schedule.data.InsertedTripStore

class ScheduleViewModel(
    private val repository: ScheduleRepository,
    private val appContext: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScheduleUiState(isLoading = true))
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    private var currentDailySchedule: DailySchedule? = null
    private var selectedViewMode: TripViewMode = TripViewMode.ACTIVE

    private val insertedTrips = mutableStateListOf<Trip>()

    init {
        loadDate(LocalDate.now())
    }

    fun selectDate(date: LocalDate) {
        loadDate(date)
    }

    fun selectViewMode(viewMode: TripViewMode) {
        selectedViewMode = viewMode
        val schedule = currentDailySchedule ?: return

        val mergedSchedule = mergedScheduleWithInsertedTrips(schedule)

        _uiState.value = ScheduleUiMapper.map(
            dailySchedule = mergedSchedule,
            selectedViewMode = selectedViewMode,
            isLoading = false,
            errorMessage = null
        )
    }

    private fun navigateBy(offset: Int) {
        val dates = _uiState.value.availableDates.sorted()
        val currentDate = _uiState.value.selectedDate

        if (dates.isEmpty()) return

        val currentIndex = dates.indexOf(currentDate)

        val targetIndex = if (currentIndex != -1) {
            currentIndex + offset
        } else {
            val insertionPoint = dates.indexOfFirst { it > currentDate }
            when {
                offset < 0 && insertionPoint == -1 -> dates.lastIndex
                offset < 0 -> (insertionPoint - 1).coerceAtLeast(0)
                offset > 0 && insertionPoint == -1 -> return
                else -> insertionPoint
            }
        }

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

    fun insertTrip(trip: Trip) {

        val activeTrip = trip.copy(status = TripStatus.ACTIVE)

        InsertedTripStore.add(appContext, activeTrip)

        reloadInsertedTripsForDate(activeTrip.date)

        val schedule = currentDailySchedule
        if (schedule == null) return


        val mergedSchedule = mergedScheduleWithInsertedTrips(schedule)

        _uiState.value = ScheduleUiMapper.map(
            dailySchedule = mergedSchedule,
            selectedViewMode = selectedViewMode,
            isLoading = false,
            errorMessage = null
        )


    }

    private fun reloadInsertedTripsForDate(date: LocalDate) {
        val loaded = InsertedTripStore.load(appContext, date)

        insertedTrips.clear()
        insertedTrips.addAll(loaded)

    }

    private fun mergedScheduleWithInsertedTrips(schedule: DailySchedule): DailySchedule {
        val insertedForDate = insertedTrips.filter { it.date == schedule.date }

        return schedule.copy(
            trips = schedule.trips + insertedForDate
        )
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

                reloadInsertedTripsForDate(date)
                val mergedSchedule = mergedScheduleWithInsertedTrips(dailySchedule)

                _uiState.value = ScheduleUiMapper.map(
                    dailySchedule = mergedSchedule,
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