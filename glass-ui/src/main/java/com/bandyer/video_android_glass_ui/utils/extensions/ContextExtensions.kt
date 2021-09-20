package com.bandyer.video_android_glass_ui.utils.extensions

import android.content.Context
import androidx.annotation.StyleRes
import androidx.annotation.StyleableRes

object ContextExtensions {
    /**
     * Get the style related to a call theme attribute
     * @receiver Context
     * @param styleAttribute the attribute for which you want to retrieve the style
     * @return The style relative to the styleAttribute
     */
    fun Context.getThemeAttribute(@StyleRes theme: Int, @StyleableRes styleable: IntArray, @StyleableRes styleAttribute: Int): Int {
        val ta = obtainStyledAttributes(theme, styleable)
        val value = if(ta.hasValue(styleAttribute))
            ta.getResourceId(styleAttribute, 0) else 0
        ta.recycle()
        return value
    }
}