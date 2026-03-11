package com.example.vtsdaily3.feature_schedule.data

class FixedScheduleFolderProvider(
    private val uriString: String?
) : ScheduleFolderProvider {

    override fun getScheduleFolderUriString(): String? = uriString
}