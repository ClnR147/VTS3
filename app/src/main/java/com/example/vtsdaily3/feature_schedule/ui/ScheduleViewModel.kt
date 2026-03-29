package com.example.vtsdaily3.feature_schedule.ui

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vtsdaily3.feature_schedule.data.InsertedTripStore
import com.example.vtsdaily3.feature_schedule.data.ScheduleRepository
import com.example.vtsdaily3.feature_schedule.data.TripStatusRecord
import com.example.vtsdaily3.feature_schedule.data.TripStatusStore
import com.example.vtsdaily3.feature_schedule.domain.DailySchedule
import com.example.vtsdaily3.feature_schedule.ui.state.ScheduleUiMapper
import com.example.vtsdaily3.feature_schedule.ui.state.ScheduleUiState
import com.example.vtsdaily3.model.Trip
import com.example.vtsdaily3.model.TripId
import com.example.vtsdaily3.model.TripStatus
import com.example.vtsdaily3.model.TripViewMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate


class ScheduleViewModel(
    private val appContext: Context,
    private val repository: ScheduleRepository,
    private val tripStatusStore: TripStatusStore
) : ViewModel() {

    enum class InsertLegType {
        PA, PR
    }

    data class TripPrefillResult(
        val phone: String = "",
        val fromAddress: String = "",
        val toAddress: String = ""
    )
    private fun normalizePrefillName(name: String): String {
        return name.trim().replace(Regex("\\s+"), " ")
    }

    private fun matchesLegType(trip: Trip, legType: InsertLegType): Boolean {
        return when (legType) {
            InsertLegType.PA -> trip.time.contains("PA", ignoreCase = true)
            InsertLegType.PR -> trip.time.contains("PR", ignoreCase = true)
        }
    }

    private fun findLastKnownTripForPassenger(
        passengerName: String,
        legType: InsertLegType
    ): Trip? {
        val targetName = normalizePrefillName(passengerName)

        val allTrips = currentDailySchedule?.trips.orEmpty()

        return allTrips
            .asSequence()
            .filter { normalizePrefillName(it.name).equals(targetName, ignoreCase = true) }
            .filter { matchesLegType(it, legType) }
            .sortedWith(
                compareByDescending<Trip> { it.date }
                    .thenByDescending { it.time }
            )
            .firstOrNull()
    }

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

        _uiState.value = ScheduleUiMapper.map(
            dailySchedule = schedule,
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
            Log.d(
                "TripStatusDebug",
                "Saving status: date=${_uiState.value.selectedDate}, tripId=${tripId.value}, status=$status"
            )
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
        refreshCurrentDate()
    }

    private fun reloadInsertedTripsForDate(date: LocalDate) {
        val loaded = InsertedTripStore.load(appContext, date)
        insertedTrips.clear()
        insertedTrips.addAll(loaded)
    }
    fun getPrefillForPassenger(
        passengerName: String,
        legType: InsertLegType
    ): TripPrefillResult? {

        val match = findLastKnownTripForPassenger(passengerName, legType)
            ?: return null

        return TripPrefillResult(
            phone = match.phone,
            fromAddress = match.fromAddress,
            toAddress = match.toAddress
        )
    }

    private fun mergedScheduleWithInsertedTrips(
        schedule: DailySchedule,
        savedStatuses: List<TripStatusRecord>
    ): DailySchedule {
        val insertedForDate = insertedTrips
            .filter { it.date == schedule.date }
            .map { insertedTrip ->
                val match = savedStatuses.firstOrNull { it.tripId == insertedTrip.id.value }
                if (match != null) {
                    insertedTrip.copy(status = match.status)
                } else {
                    insertedTrip
                }
            }

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

                reloadInsertedTripsForDate(date)

                val savedStatuses = tripStatusStore.loadStatuses(date)
                val mergedSchedule = mergedScheduleWithInsertedTrips(
                    schedule = dailySchedule,
                    savedStatuses = savedStatuses
                )

                currentDailySchedule = mergedSchedule

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