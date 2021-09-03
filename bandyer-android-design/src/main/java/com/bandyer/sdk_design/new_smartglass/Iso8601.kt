/*
 * Copyright (C) 2018 Bandyer S.r.l. All Rights Reserved.
 * See LICENSE.txt for licensing information
 */

package com.bandyer.sdk_design.new_smartglass

import android.content.Context
import android.text.format.DateUtils
import java.text.SimpleDateFormat
import java.util.*


/**
 * @suppress
 * Helper class for handling ISO 8601 strings of the following format:
 * "2008-03-01T13:00:00+01:00". It also supports parsing the "Z" timezone.
 */
object Iso8601 {

//    val twelveHoursPattern = "hh:mm a"
//    val twentyfourHoursPattern = "HH:mm"

    // Quoted "Z" to indicate UTC, no timezone offset
    private const val PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"

    private val sdf1
        get() = SimpleDateFormat(
            if (usesAmPm(Locale.getDefault())) "hh:mm a" else "HH:mm",
            Locale.getDefault()
        )
    private val sdf2
        get() = SimpleDateFormat("EEEE", Locale.getDefault())

//    private var dateFormat: SimpleDateFormat? = null

    private var bestDayFormat = ""

    private val dateFormat
        get() = SimpleDateFormat(PATTERN, Locale.getDefault()).also {
            it.timeZone = TimeZone.getTimeZone("UTC")
        }

    /**
     * Get the current time in ISO8601 format
     *
     * @return String
     */
    fun nowISO8601(): String = dateFormat.format(Date())

    /**
     * Get the current millis in UTC timezone
     *
     * @return Time in millis
     */
    fun nowUTCMillis(): Long = Calendar.getInstance(TimeZone.getTimeZone("UTC")).timeInMillis

    /**
     * Get a iso8601 formatted timestamp as millis
     *
     * @param tstamp The timestamp
     * @return Time in millis
     */
    fun getISO8601TstampInMillis(tstamp: String): Long? =
        kotlin.runCatching {
            dateFormat.parse(tstamp)?.time
        }.getOrNull()


    fun parseTimestampFromIso8601(context: Context, iso8601: Date?): String? {
        iso8601 ?: kotlin.run {
            return null
        }
        val calendar = Calendar.getInstance(Locale.getDefault())
        calendar.time = iso8601
        return parseTimestamp(context, calendar.timeInMillis)
    }

    /**
     * Transform ISO 8601 tstamp to human readable. This function has been modified to calculate UTC + offset
     */
    fun parseTimestamp(c: Context, timestamp: Long): String {
        val tmp = getDateCurrentTimeZone(timestamp)
        // check if is last week
        return if (tmp.isLastWeek()) {
            // check if today, than if yesterday, than return
            when {
                DateUtils.isToday(timestamp) -> sdf1.format(timestamp)
                timestamp >= System.currentTimeMillis() - 86400000 -> "yesterday"
//                    c.resources.getString(com.bandyer.android_chat_sdk.R.string.bandyer_chat_message_date_yesterday) + ", " + sdf1.format(
//                    timestamp)
                else -> {
                    val cal = Calendar.getInstance()
                    cal.timeInMillis = timestamp
                    sdf2.format(cal.time) + ", " + sdf1.format(timestamp)
                }
            }
        } else createDate(timestamp)
    }

    fun parseTimestampToIso8601(timestamp: Long): String {
        val date = Date()
        date.time = timestamp
        return dateFormat.format(date).toString()
    }

    fun Date.isLastWeek(): Boolean = this.time >= System.currentTimeMillis() - 604800000

    fun Date.isYesterday(): Boolean = this.time >= System.currentTimeMillis() - 86400000

    private fun getDateCurrentTimeZone(timestamp: Long): Date {
        try {
            val calendar = Calendar.getInstance(Locale.getDefault())
            calendar.time = Date(timestamp)
            return calendar.time as Date
        } catch (e: Exception) {
        }
        return Date()
    }

    /** @suppress
     * Transform ISO 8601 tstamp to human readable only displaying day. This function has been modified to calculate UTC + offset  */
    fun parseDay(c: Context, timestamp: Long): String {
        // check if is last week
        return when {
            DateUtils.isToday(timestamp) -> "today"
//                c.resources.getString(R.string.bandyer_chat_message_date_today)
            timestamp >= System.currentTimeMillis() - 86400000 -> "yesterday"
//                c.resources.getString(R.string.bandyer_chat_message_date_yesterday)
            else -> return createDateForDay(timestamp)
        }
    }

    /** @suppress
     * Transform ISO 8601 tstamp to human readable only displaying day. This function has been modified to calculate UTC + offset  */
    fun parseTime(timestamp: Long, mgtOffset: Boolean): String = sdf1.format(timestamp)

    /**
     * @suppress
     */
    private fun createDate(timestamp: Long): String =
        getModifiedDate(Locale.getDefault(), timestamp)

    /**
     * @suppress
     */
    private fun createDateForDay(timestamp: Long): String =
        getModifiedDateForDay(Locale.getDefault(), timestamp)

    /**
     * @suppress
     */
    private fun getModifiedDateForDay(locale: Locale, modified: Long): String {
        val instant = Date()
        instant.time = modified
        return java.text.DateFormat.getDateInstance(java.text.DateFormat.LONG, locale)
            .format(instant)
    }

    /**
     * @suppress
     */
    private fun getModifiedDate(locale: Locale, modified: Long): String {
//        if (dateFormat == null)
//            dateFormat = SimpleDateFormat(getDateFormat(locale), Locale.getDefault())

        val instant = Date().apply {
            time = modified
        }
        return java.text.DateFormat.getDateTimeInstance(
            java.text.DateFormat.SHORT,
            java.text.DateFormat.SHORT,
            locale
        ).format(instant)
//        return java.text.DateFormat.getDateInstance(java.text.DateFormat.SHORT, locale)
//            .format(instant) + " " + dateFormat!!.format(Date(modified))
    }

    /**
     * @suppress
     */
//    private fun getDateFormat(locale: Locale): String {
//        if (bestDayFormat == "")
//            bestDayFormat = DateFormat.getBestDateTimePattern(
//                locale,
//                if (usesAmPm(Locale.getDefault())) twelveHoursPattern else twentyfourHoursPattern
//            )
//        return bestDayFormat
//    }


    /**
     * @suppress
     */
    private fun usesAmPm(locale: Locale): Boolean =
        (SimpleDateFormat.getTimeInstance(
            java.text.DateFormat.FULL,
            locale
        ) as? SimpleDateFormat)?.toPattern()?.contains("a") == true

}
