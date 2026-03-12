package com.example.vtsdaily3.feature_schedule.di

import android.content.Context
import com.example.vtsdaily3.feature_schedule.data.AndroidScheduleFileCatalog
import com.example.vtsdaily3.feature_schedule.data.JsonTripStatusStore
import com.example.vtsdaily3.feature_schedule.data.PoiXlsTripParser
import com.example.vtsdaily3.feature_schedule.data.PrefsScheduleFolderProvider
import com.example.vtsdaily3.feature_schedule.data.RealXlsScheduleLoader
import com.example.vtsdaily3.feature_schedule.data.ScheduleRepositoryImpl
import com.example.vtsdaily3.feature_schedule.ui.ScheduleViewModelFactory

object ScheduleModule {

    fun createViewModelFactory(context: Context): ScheduleViewModelFactory {
        val appContext = context.applicationContext

        val repository = ScheduleRepositoryImpl(
            loader = RealXlsScheduleLoader(
                fileCatalog = AndroidScheduleFileCatalog(
                    context = appContext,
                    folderProvider = PrefsScheduleFolderProvider(appContext)
                ),
                tripParser = PoiXlsTripParser(appContext)
            ),
            statusStore = JsonTripStatusStore(appContext)
        )

        return ScheduleViewModelFactory(repository)
    }
}