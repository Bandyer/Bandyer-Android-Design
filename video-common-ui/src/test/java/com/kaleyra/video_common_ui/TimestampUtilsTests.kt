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

package com.kaleyra.video_common_ui

import com.kaleyra.video_common_ui.utils.TimestampUtils
import com.kaleyra.video_common_ui.utils.TimestampUtils.areDateDifferenceGreaterThanMillis
import com.kaleyra.video_utils.assertIsTrue
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