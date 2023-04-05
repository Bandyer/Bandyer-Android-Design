/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.collaboration_suite_core_ui.utils

import android.content.Context
import android.os.Build
import com.kaleyra.collaboration_suite_core_ui.R
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.Locale
import java.util.TimeZone

/**
 * @suppress
 * Helper class for handling ISO 8601 strings of the following format:
 * "2008-03-01T13:00:00+01:00". It also supports parsing the "Z" timezone.
 */
object Iso8601 {

    private const val DAY_MILLIS = 86400000L
    private const val WEEK_MILLIS = 604800000L

    /**
     * Get the current time in ISO8601 format
     *
     * @return String
     */
    fun nowISO8601(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) Instant.now().truncatedTo(ChronoUnit.MILLIS).toString()
        else GregorianCalendar.getInstance().parseToISO8601()
    }

    /**
     * Get the current millis in UTC timezone
     *
     * @return Time in millis
     */
    fun nowUTCMillis(): Long {
        return if (Build.VERSION .SDK_INT >= Build.VERSION_CODES.O) Instant.now().toEpochMilli()
        else GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC")).timeInMillis
    }

    /**
     * Get a iso8601 formatted timestamp as millis
     *
     * @param tstamp The timestamp
     * @return Time in millis
     */
    fun getISO8601TstampInMillis(tstamp: String): Long {
        return if (Build.VERSION .SDK_INT >= Build.VERSION_CODES.O) Instant.parse(tstamp).toEpochMilli()
        else {
            kotlin.runCatching {
                val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                df.timeZone = TimeZone.getTimeZone("UTC")
                df.parse(tstamp)!!.time
            }.getOrDefault(0L)
        }
    }

    /**
     * Parse a millis timestamp into ISO8601 string
     *
     * @param millis The timestamp
     * @return String The ISO8601 pattern string
     */
    fun parseMillisToIso8601(millis: Long): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) Instant.ofEpochMilli(millis).toString()
        else GregorianCalendar.getInstance().parseToISO8601(Date(millis))
    }

    /**
     * Parse a UTC millis timestamp into a human readable timestamp. This function takes into account the current zone offset.
     *
     * @param context The context
     * @param timestamp The timestamp in millis
     * @return String A human readable date time timestamp string
     */
    fun parseTimestamp(context: Context, timestamp: Long): String =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) parseTimestampApi26(context, timestamp)
        else parseTimestampApi21(context, timestamp)

    private fun parseTimestampApi26(context: Context, timestamp: Long): String {
        val zonedDateTime = Instant
            .ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())

        return if (zonedDateTime.isLastWeek()) {
            when {
                zonedDateTime.isToday() -> parseTime(timestamp)
                zonedDateTime.isYesterday() -> context.resources.getString(R.string.kaleyra_yesterday) + ", " + parseTime(timestamp)
                else -> zonedDateTime.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault()).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } + ", " + parseTime(timestamp)
            }
        } else DateTimeFormatter
            .ofLocalizedDateTime(FormatStyle.SHORT)
            .withLocale(Locale.getDefault())
            .withZone(ZoneId.systemDefault())
            .format(Instant.ofEpochMilli(timestamp))
    }

    private fun parseTimestampApi21(context: Context, timestamp: Long): String {
        val calendar = GregorianCalendar.getInstance()
        calendar.time = Date(timestamp)
        calendar.timeZone = TimeZone.getDefault()

        return if (calendar.isLastWeek()) {
            when {
                calendar.isToday() -> parseTime(timestamp)
                calendar.isYesterday() -> context.resources.getString(R.string.kaleyra_yesterday) + ", " + parseTime(timestamp)
                else -> {
                    val formatter = SimpleDateFormat("EEEE", Locale.getDefault())
                    formatter.format(calendar.time).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } + ", " + parseTime(timestamp)
                }
            }
        } else {
            val df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
            df.timeZone = calendar.timeZone
            df.format(calendar.time)
        }
    }

    /**
     * Parse a UTC millis timestamp into a human readable day. This function takes into account the current zone offset.
     *
     * @param context The context
     * @param timestamp The timestamp in millis
     * @return String A human readable date day string
     */
    fun parseDay(context: Context, timestamp: Long): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) parseDayApi26(context, timestamp)
        else parseDayApi21(context, timestamp)
    }

    private fun parseDayApi26(context: Context, timestamp: Long): String {
        val zonedDateTime = Instant
            .ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())

        return when {
            zonedDateTime.isToday() -> context.resources.getString(R.string.kaleyra_today)
            zonedDateTime.isYesterday() -> context.resources.getString(R.string.kaleyra_yesterday)
            else -> DateTimeFormatter
                .ofLocalizedDate(FormatStyle.LONG)
                .withLocale(Locale.getDefault())
                .withZone(ZoneId.systemDefault())
                .format(Instant.ofEpochMilli(timestamp))
        }
    }

    private fun parseDayApi21(context: Context, timestamp: Long): String {
        val calendar = GregorianCalendar.getInstance()
        calendar.time = Date(timestamp)
        calendar.timeZone = TimeZone.getDefault()

        return when {
            calendar.isToday() -> context.resources.getString(R.string.kaleyra_today)
            calendar.isYesterday() -> context.resources.getString(R.string.kaleyra_yesterday)
            else ->  {
                val df = DateFormat.getDateInstance(DateFormat.LONG)
                df.timeZone = calendar.timeZone
                df.format(calendar.time)
            }
        }
    }

    /**
     * Parse a UTC millis timestamp into a human readable time. This function takes into account the current zone offset.
     *
     * @param timestamp The timestamp in millis
     * @return String A human readable date day string
     */
    fun parseTime(timestamp: Long): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DateTimeFormatter
                .ofLocalizedTime(FormatStyle.SHORT)
                .withLocale(Locale.getDefault())
                .withZone(ZoneId.systemDefault())
                .format(Instant.ofEpochMilli(timestamp))
        } else {
            val calendar = GregorianCalendar.getInstance(TimeZone.getDefault(), Locale.getDefault())
            calendar.time = Date(timestamp)
            val df = DateFormat.getTimeInstance(DateFormat.SHORT)
            df.timeZone = calendar.timeZone
            df.format(calendar.time)
        }
    }

    /**
     * ZonedDateTime extension function. Check if the instant resides in the last week time period.
     *
     * @receiver Instant
     * @return Boolean True is the ZonedDateTime resides in the last week time period, false otherwise
     */
    fun ZonedDateTime.isLastWeek(): Boolean =
        this.isAfter(
            ZonedDateTime
                .now(this.zone)
                .minus(WEEK_MILLIS, ChronoUnit.MILLIS)
                .setMidnight()
        )

    /**
     * ZonedDateTime extension function. Check if the instant resides in the yesterday time period.
     *
     * @receiver Instant
     * @return Boolean True is the ZonedDateTime resides in the yesterday time period, false otherwise
     */
    fun ZonedDateTime.isYesterday(): Boolean =
        this.isAfter(
            ZonedDateTime
                .now(this.zone)
                .minus(DAY_MILLIS, ChronoUnit.MILLIS)
                .setMidnight()
        ) && !this.isToday()

    /**
     * ZonedDateTime extension function. Check if the instant resides in the current day time period.
     *
     * @receiver Instant
     * @return Boolean True is the ZonedDateTime resides in the current day time period, false otherwise
     */
    fun ZonedDateTime.isToday(): Boolean =
        this.isAfter(
            ZonedDateTime
                .now(this.zone)
                .setMidnight()
        )

    /**
     * Calendar extension function. Check if the calendar resides in the last week time period.
     *
     * @receiver Instant
     * @return Boolean True is the Calendar resides in the last week time period, false otherwise
     */
    fun Calendar.isLastWeek(): Boolean {
        return time.after(Date(GregorianCalendar.getInstance(this.timeZone).setMidnight().timeInMillis - WEEK_MILLIS))
    }

    /**
     * Calendar extension function. Check if the calendar resides in the yesterday time period.
     *
     * @receiver Instant
     * @return Boolean True is the Calendar resides in the yesterday time period, false otherwise
     */
    fun Calendar.isYesterday(): Boolean {
        return time.after(Date(GregorianCalendar.getInstance(this.timeZone).setMidnight().timeInMillis - DAY_MILLIS))
    }

    /**
     * Calendar extension function. Check if the calendar resides in the current day time period.
     *
     * @receiver Instant
     * @return Boolean True is the Calendar resides in the current day time period, false otherwise
     */
    fun Calendar.isToday(): Boolean {
        return time.after(Date(GregorianCalendar.getInstance(this.timeZone).setMidnight().timeInMillis))
    }

    private fun Calendar.setMidnight() = this.apply {
        this[Calendar.HOUR_OF_DAY] = 0
        this[Calendar.MINUTE] = 0
        this[Calendar.SECOND] = 0
        this[Calendar.MILLISECOND] = 0
    }

    private fun ZonedDateTime.setMidnight() =
        this.withHour(0)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)

    private fun Calendar.parseToISO8601(date: Date = time): String {
        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        df.timeZone = TimeZone.getTimeZone("UTC")
        return df.format(date)
    }

}
