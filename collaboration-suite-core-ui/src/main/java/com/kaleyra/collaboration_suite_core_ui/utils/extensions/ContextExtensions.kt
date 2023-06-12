/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *           
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.collaboration_suite_core_ui.utils.extensions

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Point
import android.hardware.display.DisplayManager
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Rational
import android.view.Display
import android.view.View
import android.view.WindowManager
import androidx.annotation.StyleRes
import androidx.annotation.StyleableRes
import androidx.fragment.app.FragmentActivity
import com.kaleyra.collaboration_suite_core_ui.utils.MathUtils
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.UriExtensions.getMimeType


/**
 * Context extensions
 */
object ContextExtensions {

    private val dipsMap = HashMap<Float, Int>()
    private val pixelsMap = HashMap<Float, Int>()

    /**
     * Check if the current layout configuration is RTL
     *
     * @receiver Context
     * @return Boolean True if the layout is rtl, false otherwise
     */
    fun Context.isRTL(): Boolean = resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL

    /**
     * Get the activity related to the context
     * @receiver Context
     * @return The context's activity, if it can be retrieved, null otherwise
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Activity> Context.getActivity(): T? {
        return when (this) {
            is FragmentActivity -> this as T?
            is Activity         -> this as T?
            is ContextWrapper   -> this.baseContext.getActivity() as T?
            else                -> null
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
     * Calculates the screen aspect ratio
     * @receiver Context
     * @return Rational
     */
    fun Context.getScreenAspectRatio(): Rational {
        val width = resources.displayMetrics.widthPixels
        val height = resources.displayMetrics.heightPixels
        val gcd = MathUtils.findGreatestCommonDivisor(width, height)
        return Rational(width / gcd, height / gcd)
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

    /**
     * Check if the screen is off
     *
     * @receiver Context
     * @return True if the screen is off, false otherwise
     */
    internal fun Context.isScreenOff(): Boolean = (getSystemService(Context.DISPLAY_SERVICE) as DisplayManager).displays.all { it.state != Display.STATE_ON }

    /**
     * Check if the device orientation is landscape
     *
     * @receiver Context
     * @return True if the device is in landscape, false otherwise
     */
    internal fun Context.isOrientationLandscape(): Boolean = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    /**
     * Turn on the screen, it is needed for the notifications on some devices
     *
     * @receiver Context
     */
    internal fun Context.turnOnScreen() {
        if (!isScreenOff()) return
        val pm =
            applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wl = pm.newWakeLock(
            PowerManager.SCREEN_DIM_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            javaClass.name
        )
        wl.acquire(3000)
    }

    /**
     * Check if the device is in silent mode
     *
     * @receiver Context context
     * @return Boolean true if is silent, false otherwise
     */
    fun Context.isSilent(): Boolean {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager?
        return audioManager?.ringerMode == AudioManager.RINGER_MODE_SILENT
    }

    /**
     * Check if the device is in the dnd mode
     *
     * @receiver Context context
     * @return Boolean true if is in dnd, false otherwise
     */
    fun Context.isDND(): Boolean {
        return try {
            val zenValue = Settings.Global.getInt(contentResolver, "zen_mode")
            zenValue == 1 || zenValue == 2
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Used to restart the app.
     **/
    fun Context.goToLaunchingActivity() {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        val componentName = intent!!.component
        val mainIntent = Intent.makeRestartActivityTask(componentName)
        startActivity(mainIntent)
    }

    fun Context.doesFileExists(uri:   Uri): Boolean =
        kotlin.runCatching {
            this.contentResolver.query(uri, null, null, null, null)?.use {
                it.moveToFirst()
            }
        }.getOrNull() ?: false

    fun Context.tryToOpenFile(
        uri: Uri,
        onFailure: (doesFileExists: Boolean) -> Unit
    ) {
        if (doesFileExists(uri)) tryToOpenFile(context = this, uri = uri, onFailure = { onFailure(true) })
        else onFailure(false)
    }

    private fun tryToOpenFile(context: Context, uri: Uri, onFailure: () -> Unit) {
        runCatching {
            val mimeType = uri.getMimeType(context)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            context.startActivity(intent)
        }.onFailure {
            onFailure.invoke()
        }
    }
}

