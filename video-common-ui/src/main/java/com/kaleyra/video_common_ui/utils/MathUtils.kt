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

package com.kaleyra.video_common_ui.utils

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