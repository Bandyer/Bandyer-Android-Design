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

import com.kaleyra.video_common_ui.utils.MathUtils
import org.junit.Assert.assertEquals
import org.junit.Test

class MathUtilsTest {

    @Test
    fun testFindGreatestCommonDivisor1() {
        val gcd = MathUtils.findGreatestCommonDivisor(0, 0)
        assertEquals(0, gcd)
    }

    @Test
    fun testFindGreatestCommonDivisor2() {
        val gcd = MathUtils.findGreatestCommonDivisor(0, 12)
        assertEquals(12, gcd)
    }

    @Test
    fun testFindGreatestCommonDivisor3() {
        val gcd = MathUtils.findGreatestCommonDivisor(12, 0)
        assertEquals(12, gcd)
    }

    @Test
    fun testFindGreatestCommonDivisor4() {
        val gcd = MathUtils.findGreatestCommonDivisor(178, 178)
        assertEquals(178, gcd)
    }

    @Test
    fun testFindGreatestCommonDivisor5() {
        val gcd = MathUtils.findGreatestCommonDivisor(1920, 1080)
        assertEquals(120, gcd)
    }

    @Test
    fun testFindGreatestCommonDivisor6() {
        val gcd = MathUtils.findGreatestCommonDivisor(33, 98)
        assertEquals(1, gcd)
    }
}