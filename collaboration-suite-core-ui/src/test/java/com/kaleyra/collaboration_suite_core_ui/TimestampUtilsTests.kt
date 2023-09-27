package com.kaleyra.collaboration_suite_core_ui

import com.kaleyra.collaboration_suite_core_ui.utils.TimestampUtils
import com.kaleyra.collaboration_suite_core_ui.utils.TimestampUtils.areDateDifferenceGreaterThanMillis
import com.kaleyra.collaboration_suite_utils.assertIsTrue
import junit.framework.TestCase.assertEquals
import org.junit.Test
import java.time.Instant
import java.util.*

class TimestampUtilsTests {

    @Test
    fun twoTimestamps_isSameDay_true() {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendar.set(2021, 8, 3, 16, 24, 0)
        val timestamp1 = calendar.timeInMillis
        val timestamp2 = timestamp1 - 3600
        val timestamp3 = timestamp1 - 86400000

        assertIsTrue(TimestampUtils.isSameDay(timestamp1, timestamp2))
        assertIsTrue(!TimestampUtils.isSameDay(timestamp1, timestamp3))
    }

    @Test
    fun firstDateGreaterThanDelta_areDateDifferenceGreaterThanMillis_true() {
        val millis = 3 * 60 * 1000L
        val now = Instant.now()
        val date1 = Date(now.toEpochMilli())
        val date2 = Date(now.minusMillis(millis + 1).toEpochMilli())
        assertEquals(true, areDateDifferenceGreaterThanMillis(firstDate = date2, secondDate = date1, millis = millis))
    }

    @Test
    fun secondDateGreaterThanDelta_areDateDifferenceGreaterThanMillis_true() {
        val millis = 3 * 60 * 1000L
        val now = Instant.now()
        val date1 = Date(now.toEpochMilli())
        val date2 = Date(now.minusMillis(millis + 1).toEpochMilli())
        assertEquals(true, areDateDifferenceGreaterThanMillis(firstDate = date1, secondDate = date2, millis = millis))
    }

    @Test
    fun firstDateLesserThanDelta_areDateDifferenceGreaterThanMillis_false() {
        val millis = 3 * 60 * 1000L
        val now = Instant.now()
        val date1 = Date(now.toEpochMilli())
        val date2 = Date(now.minusMillis(millis - 1).toEpochMilli())
        assertEquals(false, areDateDifferenceGreaterThanMillis(firstDate = date2, secondDate = date1, millis = millis))
    }

    @Test
    fun secondDateLesserThanDelta_areDateDifferenceGreaterThanMillis_false() {
        val millis = 3 * 60 * 1000L
        val now = Instant.now()
        val date1 = Date(now.toEpochMilli())
        val date2 = Date(now.minusMillis(millis - 1).toEpochMilli())
        assertEquals(false, areDateDifferenceGreaterThanMillis(firstDate = date1, secondDate = date2, millis = millis))
    }
}