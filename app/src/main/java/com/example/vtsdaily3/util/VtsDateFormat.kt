package com.example.vtsdaily3.util

import java.time.LocalDate

object VtsDateFormat {

    fun mmddyyyy(date: LocalDate): String {
        return "%02d-%02d-%04d".format(
            date.monthValue,
            date.dayOfMonth,
            date.year
        )
    }
}
