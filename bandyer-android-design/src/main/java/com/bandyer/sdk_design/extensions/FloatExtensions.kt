package com.bandyer.sdk_design.extensions

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