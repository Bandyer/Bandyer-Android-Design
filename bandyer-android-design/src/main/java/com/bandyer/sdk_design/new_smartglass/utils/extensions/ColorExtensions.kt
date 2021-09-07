package com.bandyer.sdk_design.new_smartglass.utils.extensions

import android.graphics.Color
import androidx.annotation.ColorInt

/**
 * Darken the receiver color by a given factor
 *
 * @receiver The target color
 * @param darkenFactor Float
 * @return The darkened color
 */
@ColorInt
fun @receiver:ColorInt Int.darkenColor(darkenFactor: Float): Int {
    return Color.HSVToColor(FloatArray(3).apply {
        Color.colorToHSV(this@darkenColor, this)
        this[2] *= darkenFactor
    })
}