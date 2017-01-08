package com.academiaexpress.Utils

import android.content.Context
import android.content.Intent.getIntent
import com.academiaexpress.R
import java.text.ParseException
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

    fun getCalendarFromString(str: String): Calendar? {
        try {
            val calendar = Calendar.getInstance()
            val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).parse(
                    str.split("\\.".toRegex()).dropLastWhile(String::isEmpty).toTypedArray()[0])
            calendar.time = date

            return calendar
        } catch (e: Exception) {
            return null
        }
    }

    fun getWeekString(context: Context, number: Int): String {
        var month = ""

        when (number) {
            Calendar.MONDAY -> month = context.getString(R.string.monday_code)
            Calendar.TUESDAY -> month = context.getString(R.string.tuesday_code)
            Calendar.WEDNESDAY -> month = context.getString(R.string.wednesday_code)
            Calendar.THURSDAY -> month = context.getString(R.string.thursday_code)
            Calendar.FRIDAY -> month = context.getString(R.string.friday_code)
            Calendar.SATURDAY -> month = context.getString(R.string.saturday_code)
            Calendar.SUNDAY -> month = context.getString(R.string.sunday_code)
            else -> return month
        }

        return month
    }

    fun isToday(firstDate: Calendar, secondDate: Calendar): Boolean {
        return firstDate.get(Calendar.YEAR) == secondDate.get(Calendar.YEAR) &&
                firstDate.get(Calendar.DAY_OF_YEAR) == secondDate.get(Calendar.DAY_OF_YEAR)
    }

    fun getCalendarDate(context: Context, openTime: String): String {
        val calendar = Calendar.getInstance()
        val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).parse(
                openTime.split("\\.".toRegex()).dropLastWhile(String::isEmpty).toTypedArray()[0])
        calendar.time = date

        if (isToday(calendar, Calendar.getInstance())) return context.getString(R.string.today_code)
        else return getWeekString(context, calendar.get(Calendar.DAY_OF_WEEK))
    }
}
