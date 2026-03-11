package com.example.vtsdaily3.feature_schedule.ui


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.vtsdaily3.feature_schedule.data.ScheduleRepository

class ScheduleViewModelFactory(
    private val repository: ScheduleRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScheduleViewModel::class.java)) {
            return ScheduleViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}