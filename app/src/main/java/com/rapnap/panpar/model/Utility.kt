package com.rapnap.panpar.model

import java.text.SimpleDateFormat
import java.util.*

object Utility {

    fun getDate(milliSeconds: Long, dateFormat: String = "dd/MM/yyyy"): String {
        // Create a DateFormatter object for displaying date in specified format.
        val formatter = SimpleDateFormat(dateFormat)

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        val calendar = Calendar.getInstance()
        calendar.setTimeInMillis(milliSeconds)
        return formatter.format(calendar.time)
    }

    fun getDate(date: Date, dateFormat: String = "dd/MM/yyyy"): String {

        return getDate(date.time, dateFormat)

    }

    fun getClearedUtc(): Calendar {
        val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        utc.clear()
        return utc
    }



}
