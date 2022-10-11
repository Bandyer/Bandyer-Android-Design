package com.kaleyra.collaboration_suite_core_ui

import com.kaleyra.collaboration_suite_core_ui.utils.TimestampUtils
import com.kaleyra.collaboration_suite_utils.assertIsTrue
import org.junit.Test
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
}