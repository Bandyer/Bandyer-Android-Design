package com.bandyer.sdk_design.extensions

object FloatExtensions {

    /**
     *  Round the float number to the @param[decimals] digit
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
}