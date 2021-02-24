/*
 * Copyright 2021-2022 Bandyer @ https://www.bandyer.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.bandyer.sdk_design.extensions

import android.app.Activity
import android.app.AppOpsManager
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import androidx.annotation.StyleableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.bandyer.sdk_design.R
import com.google.android.material.textview.MaterialTextView
import java.util.*
import kotlin.math.min

private val dipsMap = HashMap<Float, Int>()
private val pixelsMap = HashMap<Float, Int>()

/**
 * Extension function used to retrieve an attribute resource from context.
 * @param attrRes attribute resource
 * @return drawable
 */
internal fun Context.getDrawableFromAttrRes(attrRes: Int): Drawable? {
    val a = obtainStyledAttributes(intArrayOf(attrRes))
    val drawable: Drawable?
    try {
        drawable = ContextCompat.getDrawable(this, a.getResourceId(0, 0))
    } finally {
        a.recycle()
    }
    return drawable
}

/**
 * Check if device is a tablet
 */
fun Context.isTablet(): Boolean {
    return resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE
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
 * @suppress
 * @receiver Context
 * @return Boolean
 */
internal fun Context.isRtl() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == View.LAYOUT_DIRECTION_RTL
        else
            false

/**
 * Calculates screen's ratio
 * @return ratio
 */
fun Context.getScreenRatio(): Float {
    val metrics = resources.displayMetrics
    return metrics.heightPixels.toFloat() / metrics.widthPixels.toFloat()
}

/**
 * Check if can draw over apps
 * @receiver Context
 * @return Boolean true if can draw overlays, false otherwise
 */
fun Context.canDrawOverlays(): Boolean = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Settings.canDrawOverlays(this.applicationContext) else true

/**
 * Checks if this context can be or is in picture in picture mode
 * @receiver Context
 * @return Boolean true if currently in picture in picture mode, false otherwise
 */
fun Activity.isInPictureInPictureModeCompat(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val canGoInPip = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
        canGoInPip && (this as? AppCompatActivity)?.isInPictureInPictureMode == true
    } else false
}

/**
 * Interrupts watching permission change with app ops manager
 * @receiver Context
 * @param callback Function2<String, String, Unit> the callback to be stopped.
 */
fun Context.stopAppOpsWatch(callback: ((String, String) -> Unit)) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
    val appOpsManager = this.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    appOpsManager.stopWatchingMode(callback)
}

/**
 * Starts watch permission change with app ops manager
 * @receiver Context
 * @param operation String
 * @param callback Function2<String, String, Unit> the callback to be called.
 */
fun Context.startAppOpsWatch(operation: String, callback: ((String, String) -> Unit)){
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
    val pckName = applicationContext.packageName
    val appOpsManager = this.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    appOpsManager.startWatchingMode(operation, pckName, callback)
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
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
        display?.getRealSize(size)
    } else {
        display?.getSize(size)
    }
    return size
}

/**
 * Finds the fragment activity associated to the context if any.
 * @receiver Context
 * @return Activity?
 */
fun Context.scanForFragmentActivity(): androidx.fragment.app.FragmentActivity? {
    return when (this) {
        is AppCompatActivity -> this
        is androidx.fragment.app.FragmentActivity -> this
        is ContextWrapper -> this.baseContext.scanForFragmentActivity()
        else -> null
    }
}

/**
 * Get the style for the specified chat info element
 * @receiver Context
 * @param styleAttribute the attribute which defines the style of chat info element
 * @return The style relative to the styleAttribute
 */
fun Context.getChatInfoStyle(@StyleableRes styleAttribute: Int): Int {
    val ta = obtainStyledAttributes(R.style.BandyerSDKDesign_ChatInfo, R.styleable.BandyerSDKDesign_ChatInfo)
    val value = if(ta.hasValue(styleAttribute))
        ta.getResourceId(styleAttribute, 0) else 0
    ta.recycle()
    return value
}

/**
 * Get the value of an integer attribute relative to the bouncing dots style
 * @receiver Context
 * @param styleAttribute the bouncing dots attribute for which you want to retrieve the value
 * @return The value relative to the attribute
 */
fun Context.getBouncingDotsIntAttribute(@StyleableRes styleAttribute: Int): Int {
    val ta = obtainStyledAttributes(getChatInfoStyle(R.styleable.BandyerSDKDesign_ChatInfo_bandyer_bouncingDotsStyle), R.styleable.BandyerSDKDesign_TextView_Subtitle_BouncingDots)
    val value = if(ta.hasValue(styleAttribute))
        ta.getInt(styleAttribute, 0) else 0
    ta.recycle()
    return value
}

/**
 * Get the value of a boolean attribute relative to the bouncing dots style
 * @receiver Context
 * @param styleAttribute the bouncing dots attribute for which you want to retrieve the value
 * @return The value relative to the attribute
 */
fun Context.getBouncingDotsBooleanAttribute(@StyleableRes styleAttribute: Int): Boolean {
    val ta = obtainStyledAttributes(getChatInfoStyle(R.styleable.BandyerSDKDesign_ChatInfo_bandyer_bouncingDotsStyle), R.styleable.BandyerSDKDesign_TextView_Subtitle_BouncingDots)
    val value = if(ta.hasValue(styleAttribute))
        ta.getBoolean(styleAttribute, false) else false
    ta.recycle()
    return value
}

/**
 * Get the value of a dimension attribute relative to the bouncing dots style
 * @receiver Context
 * @param styleAttribute the bouncing dots attribute for which you want to retrieve the value
 * @return The value relative to the attribute
 */
fun Context.getBouncingDotsDimensionAttribute(@StyleableRes styleAttribute: Int): Float {
    val ta = obtainStyledAttributes(getChatInfoStyle(R.styleable.BandyerSDKDesign_ChatInfo_bandyer_bouncingDotsStyle), R.styleable.BandyerSDKDesign_TextView_Subtitle_BouncingDots)
    val value = if(ta.hasValue(styleAttribute))
        ta.getDimension(styleAttribute, 0f) else 0f
    ta.recycle()
    return value
}

/**
 * Get the style for the specified audio route item
 * @receiver Context
 * @param styleAttribute the audio route item attribute for which you want to retrieve the style
 * @return The style relative to the styleAttribute
 */
fun Context.getAudioRouteItemStyle(@StyleableRes styleAttribute: Int): Int {
    val ta = obtainStyledAttributes(getCallThemeAttribute(R.styleable.BandyerSDKDesign_Theme_Call_bandyer_bottomSheetAudioRouteStyle), R.styleable.BandyerSDKDesign_BottomSheet_AudioRoute)
    val value = if(ta.hasValue(styleAttribute))
        ta.getResourceId(styleAttribute, 0) else 0
    ta.recycle()
    return value
}

/**
 * Get the style for the specified call action item
 * @receiver Context
 * @param styleAttribute the call action item attribute for which you want to retrieve the style
 * @return The style relative to the styleAttribute
 */
fun Context.getCallActionItemStyle(@StyleableRes styleAttribute: Int): Int {
    val ta = obtainStyledAttributes(getCallThemeAttribute(R.styleable.BandyerSDKDesign_Theme_Call_bandyer_bottomSheetCallStyle), R.styleable.BandyerSDKDesign_BottomSheet_Call)
    val value = if(ta.hasValue(styleAttribute))
        ta.getResourceId(styleAttribute, 0) else 0
    ta.recycle()
    return value
}

/**
 * Get the style for the specified smart glass call action item
 * @receiver Context
 * @param styleAttribute the call action item attribute for which you want to retrieve the style
 * @return The style relative to the styleAttribute
 */
fun Context.getSmartGlassCallActionItemStyle(@StyleableRes styleAttribute: Int): Int {
    val ta = obtainStyledAttributes(getCallThemeAttribute(R.styleable.BandyerSDKDesign_Theme_Call_bandyer_smartGlassDialogMenuStyle), R.styleable.BandyerSDKDesign_SmartGlassDialogMenu)
    val value = if(ta.hasValue(styleAttribute))
        ta.getResourceId(styleAttribute, 0) else 0
    ta.recycle()
    return value
}

/**
 * Get the style for the specified call action item
 * @receiver Context
 * @param styleAttribute the call action item attribute for which you want to retrieve the style
 * @return The style relative to the styleAttribute
 */
fun Context.getRingingActionItemStyle(@StyleableRes styleAttribute: Int): Int {
    val ta = obtainStyledAttributes(getCallThemeAttribute(R.styleable.BandyerSDKDesign_Theme_Call_bandyer_bottomSheetRingingStyle), R.styleable.BandyerSDKDesign_BottomSheet_Ringing)
    val value = if(ta.hasValue(styleAttribute))
        ta.getResourceId(styleAttribute, 0) else 0
    ta.recycle()
    return value
}

/**
 * Get the style related to a call theme attribute
 * @receiver Context
 * @param styleAttribute the attribute for which you want to retrieve the style
 * @return The style relative to the styleAttribute
 */
fun Context.getCallThemeAttribute(@StyleableRes styleAttribute: Int): Int {
    val ta = obtainStyledAttributes(R.style.BandyerSDKDesign_Theme_Call, R.styleable.BandyerSDKDesign_Theme_Call)
    val value = if(ta.hasValue(styleAttribute))
        ta.getResourceId(styleAttribute, 0) else 0
    ta.recycle()
    return value
}

/**
 * Get the style related to a whiteboard dialog attribute
 * @receiver Context
 * @param styleAttribute the attribute for which you want to retrieve the style
 * @return The style relative to the styleAttribute
 */
fun Context.getWhiteboardDialogAttribute(@StyleableRes styleAttribute: Int): Int {
    val ta = obtainStyledAttributes(getCallThemeAttribute(R.styleable.BandyerSDKDesign_Theme_Call_bandyer_whiteboardDialogStyle), R.styleable.BandyerSDKDesign_BottomSheetDialog_Whiteboard)
    val value = if(ta.hasValue(styleAttribute))
        ta.getResourceId(styleAttribute, 0) else 0
    ta.recycle()
    return value
}

/**
 * Get the style related to a text editor dialog attribute
 * @receiver Context
 * @param styleAttribute the attribute for which you want to retrieve the style
 * @return The style relative to the styleAttribute
 */
fun Context.getTextEditorDialogAttribute(@StyleableRes styleAttribute: Int): Int {
    val ta = obtainStyledAttributes(getWhiteboardDialogAttribute(R.styleable.BandyerSDKDesign_BottomSheetDialog_Whiteboard_bandyer_textEditorDialogStyle), R.styleable.BandyerSDKDesign_BottomSheetDialog_TextEditor)
    val value = if(ta.hasValue(styleAttribute))
        ta.getResourceId(styleAttribute, 0) else 0
    ta.recycle()
    return value
}

/**
 * Get the activity related to the context
 * @receiver Context
 * @return The context's activity, if it can be retrieved, null otherwise
 */
@Suppress("UNCHECKED_CAST")
fun <T: Activity> Context.getActivity(): T? {
    return when (this) {
        is FragmentActivity -> this as T?
        is Activity -> this as T?
        is ContextWrapper -> this.baseContext.getActivity() as T?
        else -> null
    }
}

/**
 * Get the minimum decorView dimension between the width and the height
 * @receiver Context
 * @return The minimum dimension between the width and the height
 */
fun Context.getMinDecorViewDimension(): Int {
    val decorView = getActivity<AppCompatActivity>()?.window?.decorView ?: return 0
    return min(decorView.width, decorView.height)
}

/**
 * Set the text appearance for a given TextView
 * @receiver Context
 * @param textView the TextView to which apply the textAppearance
 * @param resId The textAppearance style to be applied to the TextView
 */
fun Context.setTextAppearance(textView: MaterialTextView?, resId: Int) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
        textView?.setTextAppearance(this, resId)
    else
        textView?.setTextAppearance(resId)
}