package com.bandyer.video_android_core_ui.extensions

import android.graphics.Color
import androidx.annotation.ColorInt

object ColorIntExtensions {

    /**
     * Tell if a color needs to be coupled with a light color
     * @receiver Int the IntColor
     * @return true if the color needs a light color
     */
    fun @receiver:androidx.annotation.ColorInt Int.requiresLightColor(): Boolean {
        val red = Color.red(this)
        val green = Color.green(this)
        val blue = Color.blue(this)
        val yiq = ((red * 299) + (green * 587) + (blue * 114)) / 1000
        return yiq < 192
    }

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
}