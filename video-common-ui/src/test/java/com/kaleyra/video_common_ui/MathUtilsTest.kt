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