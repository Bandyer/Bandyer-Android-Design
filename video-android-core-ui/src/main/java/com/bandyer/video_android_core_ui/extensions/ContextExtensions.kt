package com.bandyer.video_android_core_ui.extensions

import android.content.Context
import android.util.DisplayMetrics
import java.util.HashMap

object ContextExtensions {

    private val dipsMap = HashMap<Float, Int>()
    private val pixelsMap = HashMap<Float, Int>()

    /**
     * Convert dp value in pixels
     * @param dp value
     * @return value in pixels
     */
    fun Context.dp2px(dp: Float): Int {
        dipsMap[dp]?.let { return it }

        val metrics = resources.displayMetrics
        val value = (dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()
        dipsMap[dp] = value

        return value
    }

    /**
     * Convert px value in dp
     * @param px value
     * @return value in dps
     */
    fun Context.px2dp(px: Float): Int {
        pixelsMap[px]?.let { return it }

        val metrics = resources.displayMetrics
        val value = (px / (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()
        pixelsMap[px] = value

        return value
    }
}