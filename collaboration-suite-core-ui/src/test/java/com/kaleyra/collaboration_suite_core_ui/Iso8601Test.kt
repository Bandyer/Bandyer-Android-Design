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

import com.kaleyra.collaboration_suite_core_ui.utils.Iso8601
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Test
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

class Iso8601Test {

    @Test
    fun iso8601Timestamp_getISO8601TstampInMillis_timestampInMillis() {
        val timestamp = "2021-09-03T16:24:00.000000Z"
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendar.set(2021, 8, 3, 16, 24, 0)
        var expected = calendar.timeInMillis
        // set milliseconds to 0
        expected -= expected % 1000
        val result = Iso8601.getISO8601TstampInMillis(timestamp)
        assertEquals(result, expected)
    }

    @Test
    fun nowISO8601_nowInIso8601() {
        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm")
        df.timeZone = TimeZone.getTimeZone("UTC")
        val expected = df.format(Calendar.getInstance().time)
        val result = Iso8601.nowISO8601()
        assert(result.contains(expected))
    }

    @Test
    fun nowUTCMillis_nowInMillis() {
        var expected = Instant.now().toEpochMilli()
        var result = Iso8601.nowUTCMillis()

        expected -= expected % 1000
        result -= result % 1000
        assertEquals(expected, result)
    }

    @Test
    fun longTimestamp_parseMillisToIso8601_iso8601String() {
        val millis = Instant
            .now()
            .minus(3, ChronoUnit.DAYS)
            .toEpochMilli()
        val expected = Instant.ofEpochMilli(millis).toString()
        val result = Iso8601.parseMillisToIso8601(millis)
        assertEquals(expected, result)
    }
}