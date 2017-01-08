package com.academiaexpress.Utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    fun getDateStringRepresentationWithoutTime(date: Date?): String {
        if (date == null) return ""
        val format = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        format.timeZone = TimeZone.getDefault()
        return format.format(date)
    }

    fun getTimeStringRepresentation(date: Date?): String {
        if (date == null) return ""
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        format.timeZone = TimeZone.getDefault()
        return format.format(date)
    }

    fun getDateFullString(date: Date?): String {
        if (date == null) return ""

        val format = SimpleDateFormat("d MMM HH:mm", Locale.getDefault())
        format.timeZone = TimeZone.getDefault()
        return format.format(date)
    }

    fun getDateStringRepresentation(date: Date): String {
        val format = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
        format.timeZone = TimeZone.getDefault()
        return format.format(date)
    }
}
