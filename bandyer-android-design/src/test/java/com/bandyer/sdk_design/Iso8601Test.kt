package com.bandyer.sdk_design

import com.bandyer.android_common.assertIsTrue
import com.bandyer.sdk_design.new_smartglass.Iso8601
import org.junit.Test
import java.util.*

class Iso8601Test {

    @Test
    fun testNowIso8601Tstamp() {
        val timestamp = "2021-09-03T16:24:00.000Z"
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendar.set(2021, 8, 3, 16, 24, 0)
        val time = calendar.timeInMillis
        // set milliseconds to 0
        val expected = time - time % 1000
        val result = Iso8601.getISO8601TstampInMillis(timestamp)
        assertIsTrue(result == expected)
    }

 }