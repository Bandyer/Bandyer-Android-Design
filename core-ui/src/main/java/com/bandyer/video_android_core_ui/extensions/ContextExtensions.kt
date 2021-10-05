package com.bandyer.video_android_core_ui.extensions

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Point
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.annotation.StyleRes
import androidx.annotation.StyleableRes
import androidx.fragment.app.FragmentActivity
import java.util.*

/**
 * Context extensions
 */
object ContextExtensions {

    private val dipsMap = HashMap<Float, Int>()
    private val pixelsMap = HashMap<Float, Int>()

    /**
     * Get the activity related to the context
     * @receiver Context
     * @return The context's activity, if it can be retrieved, null otherwise
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Activity> Context.getActivity(): T? {
        return when (this) {
            is FragmentActivity -> this as T?
            is Activity -> this as T?
            is ContextWrapper -> this.baseContext.getActivity() as T?
            else -> null
        }
    }

    /**
     * Calculates screen's size
     * @receiver Context
     * @return Point
     */
    fun Context.getScreenSize(): Point {
        val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getActivity<Activity>()?.display
        } else {
            @Suppress("DEPRECATION")
            (getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        }
        val size = Point()
        display?.getRealSize(size)
        return size
    }

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

    /**
     * Retrieve a theme attribute's style
     *
     * @receiver Context
     * @param theme The theme
     * @param styleable The styleable defining the theme's attributes
     * @param styleAttribute The attribute for which you want to retrieve the style
     * @return Int
     */
    fun Context.getThemeAttribute(
        @StyleRes theme: Int,
        @StyleableRes styleable: IntArray,
        @StyleableRes styleAttribute: Int
    ): Int {
        val ta = obtainStyledAttributes(theme, styleable)
        val value = if (ta.hasValue(styleAttribute))
            ta.getResourceId(styleAttribute, 0) else 0
        ta.recycle()
        return value
    }
}