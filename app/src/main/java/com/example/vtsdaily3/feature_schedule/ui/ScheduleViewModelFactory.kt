package com.example.vtsdaily3.feature_schedule.ui


import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.vtsdaily3.feature_schedule.data.JsonTripStatusStore
import com.example.vtsdaily3.feature_schedule.data.ScheduleRepository

class ScheduleViewModelFactory(
    private val appContext: Context,
    private val repository: ScheduleRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        val tripStatusStore = JsonTripStatusStore(appContext)

        return ScheduleViewModel(
            appContext = appContext,
            repository = repository,
            tripStatusStore = tripStatusStore
        ) as T
    }
}