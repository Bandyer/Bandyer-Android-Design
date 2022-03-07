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

package com.kaleyra.collaboration_suite_core_ui

import com.kaleyra.collaboration_suite_utils.assertIsTrue
import com.kaleyra.collaboration_suite_core_ui.utils.Iso8601
import com.kaleyra.collaboration_suite_core_ui.utils.Iso8601.isLastWeek
import com.kaleyra.collaboration_suite_core_ui.utils.Iso8601.isToday
import com.kaleyra.collaboration_suite_core_ui.utils.Iso8601.isYesterday
import org.junit.Test
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.*

class Iso8601Test {

    @Test
    fun testNowIso8601Tstamp() {
        val timestamp = "2021-09-03T16:24:00.000000Z"
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendar.set(2021, 8, 3, 16, 24, 0)
        var expected = calendar.timeInMillis
        // set milliseconds to 0
        expected -= expected % 1000
        val result = Iso8601.getISO8601TstampInMillis(timestamp)
        assertIsTrue(result == expected)
    }

    @Test
    fun testNowISO8601() {
        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm")
        df.timeZone = TimeZone.getTimeZone("UTC")
        val expected = df.format(Calendar.getInstance().time)
        val result = Iso8601.nowISO8601()
        assertIsTrue(result.contains(expected))
    }

    @Test
    fun testNowUTCMillis() {
        var expected = Instant.now().toEpochMilli()
        var result = Iso8601.nowUTCMillis()

        expected -= expected % 1000
        result -= result % 1000
        assertIsTrue(expected == result)
    }

    @Test
    fun testIsLastWeek() {
        val threeDaysAgo = ZonedDateTime
            .now(ZoneId.systemDefault())
            .minus(3, ChronoUnit.DAYS)
        val tenDaysAgo = ZonedDateTime
            .now(ZoneId.systemDefault())
            .minus(10, ChronoUnit.DAYS)
        assertIsTrue(threeDaysAgo.isLastWeek())
        assertIsTrue(!tenDaysAgo.isLastWeek())
    }

    @Test
    fun testIsYesterday() {
        val now = ZonedDateTime
            .now(ZoneId.systemDefault())
        val yesterday = now
            .minus(1, ChronoUnit.DAYS)
        val threeDaysAgo = now
            .minus(3, ChronoUnit.DAYS)
        assertIsTrue(!now.isYesterday())
        assertIsTrue(yesterday.isYesterday())
        assertIsTrue(!threeDaysAgo.isYesterday())
    }

    @Test
    fun testIsToday() {
        val now = ZonedDateTime.now(ZoneId.systemDefault())
        val yesterday = now
            .minus(1, ChronoUnit.DAYS)
        assertIsTrue(now.isToday())
        assertIsTrue(
            !yesterday.isToday()
        )
    }

    @Test
    fun testParseMillisToIso8601() {
        val millis = Instant
            .now()
            .minus(3, ChronoUnit.DAYS)
            .toEpochMilli()
        val expected = Instant.ofEpochMilli(millis).toString()
        val result = Iso8601.parseMillisToIso8601(millis)
        assertIsTrue(expected == result)
    }
}