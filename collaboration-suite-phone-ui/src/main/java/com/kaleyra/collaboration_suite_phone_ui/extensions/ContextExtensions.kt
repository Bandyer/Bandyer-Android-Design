/*
 * Copyright 2022 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.collaboration_suite_phone_ui.extensions

import android.app.Activity
import android.app.AppOpsManager
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.os.Build
import android.provider.Settings
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.StyleableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.material.textview.MaterialTextView
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ContextExtensions.getActivity
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ContextExtensions.getThemeAttribute
import com.kaleyra.collaboration_suite_phone_ui.R
import kotlin.math.min

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
 * Finds the fragment activity associated to the context if any.
 * @receiver Context
 * @return Activity?
 */
fun Context.scanForFragmentActivity(): androidx.fragment.app.FragmentActivity? {
    return when (this) {
        is AppCompatActivity -> this
        is FragmentActivity -> this
        is ContextWrapper -> this.baseContext.scanForFragmentActivity()
        else -> null
    }
}

/**
 * Get the value of an integer attribute relative to smart glass dialog menu
 * @receiver Context
 * @param styleAttribute the pager indicator attribute for which you want to retrieve the value
 * @return The value relative to the attribute
 */
fun Context.getPagerIndicatorIntAttribute(@StyleableRes styleAttribute: Int): Int {
    val ta = obtainStyledAttributes(getSmartGlassMenuAttribute(R.styleable.KaleyraCollaborationSuiteUI_SmartGlassMenu_kaleyra_pagerIndicatorStyle), R.styleable.KaleyraCollaborationSuiteUI_PagerIndicator)
    val value = if(ta.hasValue(styleAttribute))
        ta.getInt(styleAttribute, 0) else 0
    ta.recycle()
    return value
}

/**
 * Get the value of a boolean attribute relative to smart glass dialog menu
 * @receiver Context
 * @param styleAttribute the pager indicator attribute for which you want to retrieve the value
 * @return The value relative to the attribute
 */
fun Context.getPagerIndicatorBooleanAttribute(@StyleableRes styleAttribute: Int): Boolean {
    val ta = obtainStyledAttributes(getSmartGlassMenuAttribute(R.styleable.KaleyraCollaborationSuiteUI_SmartGlassMenu_kaleyra_pagerIndicatorStyle), R.styleable.KaleyraCollaborationSuiteUI_PagerIndicator)
    val value = if(ta.hasValue(styleAttribute))
        ta.getBoolean(styleAttribute, false) else false
    ta.recycle()
    return value
}


/**
 * Get the value of a color attribute relative to smart glass dialog menu
 * @receiver Context
 * @param styleAttribute the pager indicator attribute for which you want to retrieve the value
 * @return The value relative to the attribute
 */
fun Context.getPagerIndicatorColorAttribute(@StyleableRes styleAttribute: Int): Int {
    val ta = obtainStyledAttributes(getSmartGlassMenuAttribute(R.styleable.KaleyraCollaborationSuiteUI_SmartGlassMenu_kaleyra_pagerIndicatorStyle), R.styleable.KaleyraCollaborationSuiteUI_PagerIndicator)
    val value = if(ta.hasValue(styleAttribute))
        ta.getColor(styleAttribute, 0) else 0
    ta.recycle()
    return value
}

/**
 * Get the value of a pixel size attribute relative to smart glass dialog menu
 * @receiver Context
 * @param styleAttribute the pager indicator attribute for which you want to retrieve the value
 * @return The value relative to the attribute
 */
fun Context.getPagerIndicatorDimensionPixelSizeAttribute(@StyleableRes styleAttribute: Int): Int {
    val ta = obtainStyledAttributes(getSmartGlassMenuAttribute(R.styleable.KaleyraCollaborationSuiteUI_SmartGlassMenu_kaleyra_pagerIndicatorStyle), R.styleable.KaleyraCollaborationSuiteUI_PagerIndicator)
    val value = if(ta.hasValue(styleAttribute))
        ta.getDimensionPixelSize(styleAttribute, 0) else 0
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
    val ta = obtainStyledAttributes(getCallThemeAttribute(R.styleable.KaleyraCollaborationSuiteUI_Theme_Call_kaleyra_bottomSheetAudioRouteStyle), R.styleable.KaleyraCollaborationSuiteUI_BottomSheet_AudioRoute)
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
    val ta = obtainStyledAttributes(getCallThemeAttribute(R.styleable.KaleyraCollaborationSuiteUI_Theme_Call_kaleyra_bottomSheetCallStyle), R.styleable.KaleyraCollaborationSuiteUI_BottomSheet_Call)
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
fun Context.getSmartGlassMenuAttribute(@StyleableRes styleAttribute: Int): Int {
    val ta = obtainStyledAttributes(getSmartGlassMenuDialogAttribute(R.styleable.KaleyraCollaborationSuiteUI_SmartGlassDialogMenu_kaleyra_smartGlassMenuStyle), R.styleable.KaleyraCollaborationSuiteUI_SmartGlassMenu)
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
    val ta = obtainStyledAttributes(getCallThemeAttribute(R.styleable.KaleyraCollaborationSuiteUI_Theme_Call_kaleyra_bottomSheetRingingStyle), R.styleable.KaleyraCollaborationSuiteUI_BottomSheet_Ringing)
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
fun Context.getCallThemeAttribute(@StyleableRes styleAttribute: Int): Int =
    this.getThemeAttribute(
        R.style.KaleyraCollaborationSuiteUI_Theme_Call,
        R.styleable.KaleyraCollaborationSuiteUI_Theme_Call,
        styleAttribute
    )

/**
 * Get the style related to a whiteboard dialog attribute
 * @receiver Context
 * @param styleAttribute the attribute for which you want to retrieve the style
 * @return The style relative to the styleAttribute
 */
fun Context.getWhiteboardDialogAttribute(@StyleableRes styleAttribute: Int): Int {
    val ta = obtainStyledAttributes(getCallThemeAttribute(R.styleable.KaleyraCollaborationSuiteUI_Theme_Call_kaleyra_whiteboardDialogStyle), R.styleable.KaleyraCollaborationSuiteUI_BottomSheetDialog_Whiteboard)
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
    val ta = obtainStyledAttributes(getWhiteboardDialogAttribute(R.styleable.KaleyraCollaborationSuiteUI_BottomSheetDialog_Whiteboard_kaleyra_textEditorDialogStyle), R.styleable.KaleyraCollaborationSuiteUI_BottomSheetDialog_TextEditor)
    val value = if(ta.hasValue(styleAttribute))
        ta.getResourceId(styleAttribute, 0) else 0
    ta.recycle()
    return value
}

/**
 * Get the style related to a smartglass dialog menu attribute
 * @receiver Context
 * @param styleAttribute the attribute for which you want to retrieve the style
 * @return The style relative to the styleAttribute
 */
fun Context.getSmartGlassMenuDialogAttribute(@StyleableRes styleAttribute: Int): Int {
    val ta = obtainStyledAttributes(getCallThemeAttribute(R.styleable.KaleyraCollaborationSuiteUI_Theme_Call_kaleyra_smartGlassDialogMenuStyle), R.styleable.KaleyraCollaborationSuiteUI_SmartGlassDialogMenu)
    val value = if(ta.hasValue(styleAttribute))
        ta.getResourceId(styleAttribute, 0) else 0
    ta.recycle()
    return value
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