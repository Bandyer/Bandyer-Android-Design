/*
 * Copyright 2022 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.collaboration_suite_phone_ui.extensions

/**
 * Float arithmetics extension utils
 */
object FloatExtensions {

    /**
     * Rounds the given value to a float towards positive infinity with @param[decimals] digits after comma
     *
     * @receiver Float
     * @param decimals The number of digits
     * @return Float The rounded number
     */
    fun Float.round(decimals: Int): Float {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        return (kotlin.math.round(this * multiplier) / multiplier).toFloat()
    }

    /**
     * Rounds the given value to a float towards negative infinity with @param[decimals] digits after comma
     *
     * @receiver Float
     * @param decimals The number of digits
     * @return Float The rounded number
     */
    fun Float.floor(decimals: Int): Float {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        return (kotlin.math.floor(this * multiplier) / multiplier).toFloat()
    }
}