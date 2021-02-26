package com.bandyer.sdk_design.extensions

import android.graphics.Color

/**
 * Tell if a color needs a light overlay based on its luminance
 * @receiver Int the IntColor
 * @return true if the color needs a light overlay
 */
fun Int.requiresLightOverlay(): Boolean {
    val red = Color.red(this)
    val green = Color.green(this)
    val blue = Color.blue(this)
    val yiq = ((red * 299) + (green * 587) + (blue * 114)) / 1000
    return yiq < 192
}

