package com.irateam.vkplayer.util.extension

import com.irateam.vkplayer.model.Time
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

val DEFAULT_TIME_FORMATTER = SimpleDateFormat("HH:mm", Locale.getDefault())

fun calendarOf(
        hours: Int = 0,
        minutes: Int = 0,
        seconds: Int = 0): Calendar {

    val calendar = Calendar.getInstance()

    calendar.set(Calendar.HOUR_OF_DAY, hours)
    calendar.set(Calendar.MINUTE, minutes)
    calendar.set(Calendar.SECOND, seconds)

    return calendar
}

fun Calendar.toTime() = Time(
        get(Calendar.HOUR_OF_DAY),
        get(Calendar.MINUTE),
        get(Calendar.SECOND))

fun Calendar.toTimeFormatted(formatter: DateFormat = DEFAULT_TIME_FORMATTER): String {
    return formatter.format(this.time)
}