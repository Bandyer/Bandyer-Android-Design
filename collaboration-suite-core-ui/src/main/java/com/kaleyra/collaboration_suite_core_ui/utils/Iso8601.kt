/*
 * Copyright 2022 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.collaboration_suite_core_ui.utils

import android.content.Context
import com.kaleyra.collaboration_suite_core_ui.R
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.*

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
    fun nowISO8601(): String = Instant.now().toString()

    /**
     * Get the current millis in UTC timezone
     *
     * @return Time in millis
     */
    fun nowUTCMillis(): Long = Instant.now().toEpochMilli()

    /**
     * Get a iso8601 formatted timestamp as millis
     *
     * @param tstamp The timestamp
     * @return Time in millis
     */
    fun getISO8601TstampInMillis(tstamp: String): Long = Instant.parse(tstamp).toEpochMilli()

    /**
     * Parse a millis timestamp into ISO8601 string
     *
     * @param millis The timestamp
     * @return String The ISO8601 pattern string
     */
    fun parseMillisToIso8601(millis: Long): String = Instant.ofEpochMilli(millis).toString()

    /**
     * Parse a UTC millis timestamp into a human readable timestamp. This function takes into account the current zone offset.
     *
     * @param context The context
     * @param timestamp The timestamp in millis
     * @return String A human readable date time timestamp string
     */
    fun parseTimestamp(context: Context, timestamp: Long): String {
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

    /**
     * Parse a UTC millis timestamp into a human readable day. This function takes into account the current zone offset.
     *
     * @param context The context
     * @param timestamp The timestamp in millis
     * @return String A human readable date day string
     */
    fun parseDay(context: Context? = null, timestamp: Long): String {
        val zonedDateTime = Instant
            .ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())

        return when {
            context != null && zonedDateTime.isToday() -> context.resources.getString(R.string.kaleyra_today)
            context != null && zonedDateTime.isYesterday() -> context.resources.getString(R.string.kaleyra_yesterday)
            else -> DateTimeFormatter
                .ofLocalizedDate(FormatStyle.LONG)
                .withLocale(Locale.getDefault())
                .withZone(ZoneId.systemDefault())
                .format(Instant.ofEpochMilli(timestamp))
        }
    }

    /**
     * Parse a UTC millis timestamp into a human readable time. This function takes into account the current zone offset.
     *
     * @param timestamp The timestamp in millis
     * @return String A human readable date day string
     */
    fun parseTime(timestamp: Long): String =
        DateTimeFormatter
            .ofLocalizedTime(FormatStyle.SHORT)
            .withLocale(Locale.getDefault())
            .withZone(ZoneId.systemDefault())
            .format(Instant.ofEpochMilli(timestamp))

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

    private fun ZonedDateTime.setMidnight() =
        this.withHour(0)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)
}
