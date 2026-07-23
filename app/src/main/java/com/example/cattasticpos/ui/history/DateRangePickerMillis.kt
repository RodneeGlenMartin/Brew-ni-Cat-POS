package com.example.cattasticpos.ui.history

import java.util.Calendar
import java.util.TimeZone

/**
 * Material3 DateRangePicker reports selected days as UTC-midnight epoch millis whose
 * calendar fields (year/month/day) match the visible local date. Filter queries use
 * local start/end-of-day bounds. These helpers keep the two representations aligned.
 */
internal object DateRangePickerMillis {

    fun utcPickerToLocalStartOfDay(utcPickerMillis: Long): Long {
        val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = utcPickerMillis
        }
        return Calendar.getInstance().apply {
            set(Calendar.YEAR, utc.get(Calendar.YEAR))
            set(Calendar.MONTH, utc.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, utc.get(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    fun localStartOfDayToUtcPicker(localStartOfDayMillis: Long): Long {
        val local = Calendar.getInstance().apply {
            timeInMillis = localStartOfDayMillis
        }
        return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(Calendar.YEAR, local.get(Calendar.YEAR))
            set(Calendar.MONTH, local.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, local.get(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    fun localTodayStartUtcPicker(): Long {
        val localTodayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        return localStartOfDayToUtcPicker(localTodayStart)
    }
}
