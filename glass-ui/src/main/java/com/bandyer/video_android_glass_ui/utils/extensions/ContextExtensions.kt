package com.bandyer.video_android_glass_ui.utils.extensions

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.StyleableRes
import com.bandyer.video_android_core_ui.extensions.ContextExtensions.getThemeAttribute
import com.bandyer.video_android_glass_ui.R

/**
 * Context utility class
 */
object ContextExtensions {
    /**
     * Retrieve a chat theme attribute's style
     *
     * @receiver Context
     * @param styleAttribute the attribute for which you want to retrieve the style
     * @return Int the attribute's style
     */
    internal fun Context.getChatThemeAttribute(
        @StyleableRes styleAttribute: Int
    ): Int =
        this.getThemeAttribute(
            R.style.BandyerSDKDesign_Theme_DayNight_GlassChat,
            R.styleable.BandyerSDKDesign_Theme_Glass_Chat,
            styleAttribute
        )

    /**
     * Retrieve a theme attribute value's resource id
     *
     * @receiver Resources.Theme
     * @param attr Int the theme attribute
     * @return Int the resource id
     */
    internal fun Resources.Theme.getAttributeResourceId(
        @AttrRes attr: Int
    ): Int =
        TypedValue()
            .also {
                resolveAttribute(attr, it, true)
            }.resourceId
}