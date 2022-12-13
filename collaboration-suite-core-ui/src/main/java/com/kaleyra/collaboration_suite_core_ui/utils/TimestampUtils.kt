package com.kaleyra.collaboration_suite_core_ui.utils

import android.content.Context
import com.kaleyra.collaboration_suite_core_ui.R
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.*

object TimestampUtils {

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
            val resources = context.resources
            val time = parseTime(timestamp)
            when {
                zonedDateTime.isToday() -> time
                zonedDateTime.isYesterday() -> {
                    val yesterday = resources.getString(R.string.kaleyra_yesterday)
                    resources.getString(R.string.kaleyra_day_time_pattern, yesterday, time)
                }
                else -> {
                    val day = zonedDateTime.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault()).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                    resources.getString(R.string.kaleyra_day_time_pattern, day, time)
                }
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
    fun parseDay(context: Context, timestamp: Long): String {
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
     * Check if two timestamps lie in the same day
     *
     * @param timestamp1 Long The first timestamp
     * @param timestamp2 Long The second timestamp
     * @return Boolean True if the two timestamps lie in the same day, false otherwise
     */
    fun isSameDay(timestamp1: Long, timestamp2: Long): Boolean =
        parseDayShort(timestamp1).equals(parseDayShort(timestamp2))

    private fun ZonedDateTime.isLastWeek(): Boolean =
        this.isAfter(
            ZonedDateTime
                .now(this.zone)
                .minus(7, ChronoUnit.DAYS)
                .setMidnight()
        )

    private fun ZonedDateTime.isYesterday(): Boolean =
        this.isAfter(
            ZonedDateTime
                .now(this.zone)
                .minus(1, ChronoUnit.DAYS)
                .setMidnight()
        ) && !this.isToday()

    private fun ZonedDateTime.isToday(): Boolean =
        this.isAfter(
            ZonedDateTime
                .now(this.zone)
                .setMidnight()
        )

    private fun parseDayShort(timestamp: Long) =
        DateTimeFormatter
            .ofLocalizedDate(FormatStyle.SHORT)
            .withLocale(Locale.getDefault())
            .withZone(ZoneId.systemDefault())
            .format(Instant.ofEpochMilli(timestamp))

    private fun ZonedDateTime.setMidnight() =
        this.withHour(0)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)
}