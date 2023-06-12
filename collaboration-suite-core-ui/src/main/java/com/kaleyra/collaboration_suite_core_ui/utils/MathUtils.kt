package com.kaleyra.collaboration_suite_core_ui.utils

object MathUtils {

    fun findGreatestCommonDivisor(number1: Int, number2: Int): Int {
        var n1 = number1
        var n2 = number2
        if (n1 == 0) {
            return n2
        }
        if (n2 == 0) {
            return n1
        }
        var n = 0
        while (n1 or n2 and 1 == 0) {
            n1 = n1 shr 1
            n2 = n2 shr 1
            n++
        }
        while (n1 and 1 == 0) {
            n1 = n1 shr 1
        }
        do {
            while (n2 and 1 == 0) {
                n2 = n2 shr 1
            }
            if (n1 > n2) {
                val temp = n1
                n1 = n2
                n2 = temp
            }
            n2 -= n1
        } while (n2 != 0)
        return n1 shl n
    }
}