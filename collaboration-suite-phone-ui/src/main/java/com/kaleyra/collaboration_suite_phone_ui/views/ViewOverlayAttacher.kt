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

package com.kaleyra.collaboration_suite_phone_ui.views

import android.app.Activity
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ContextExtensions.getScreenSize
import com.kaleyra.collaboration_suite_phone_ui.R
import java.lang.Integer.max

/**
 * This class has the responsibility of attaching overlay views based on OverlayType type
 * @property view View the view that will be attached.
 */
class ViewOverlayAttacher(val view: View) : View.OnLayoutChangeListener {

    /**
     * Overlay View Type
     */
    enum class OverlayType {
        /**
         * The overlay view is visible upon all apps and launcher.
         */
        GLOBAL,

        /**
         * The overlay view is visible only upon current app.
         */
        CURRENT_APPLICATION
    }

    private var windowManager: WindowManager? = null
    private val inAppOverlays: MutableList<View> = mutableListOf()
    private var globalOverlay: View? = null

    /**
     * Attaches the view to the current application or over all applications with the given context with the OverlayType specified.
     * @param context Context
     * @param type OverlayType
     */
    fun attach(context: Context, type: OverlayType) {
        val contextIdentifier = getContextIdentifier(context, type)
        if (context !is Activity && type == OverlayType.CURRENT_APPLICATION) return
        when {
            type == OverlayType.GLOBAL -> {
                if (globalOverlay?.getTag(R.id.kaleyra_id_status_bar_overlay_tag) == contextIdentifier) return
                globalOverlay = view
                globalOverlay!!.setTag(R.id.kaleyra_id_status_bar_overlay_tag, contextIdentifier)
                windowManager = globalOverlay!!.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                globalOverlay!!.addOnLayoutChangeListener(this)
                runCatching { windowManager!!.addView(globalOverlay, getSystemOverlayLayoutParams()) }
            }
            context is Activity -> {
                inAppOverlays.firstOrNull { it.getTag(R.id.kaleyra_id_status_bar_overlay_tag) == contextIdentifier }?.let { return }
                val overlay = cloneOverlay(context, view)
                overlay.setTag(R.id.kaleyra_id_status_bar_overlay_tag, contextIdentifier)
                inAppOverlays.add(overlay)
                (context.window.decorView as ViewGroup).addView(overlay)
                inAppOverlays.forEach { it.requestLayout() }
            }
            else -> Unit
        }
    }

    private fun cloneOverlay(context: Context, overlay: View): View = with(overlay) { this::class.constructors.first().call(context, null, 0) }

    /**
     * Detaches all the views from its parent if has been previously attached.
     */
    fun detachAll() {
        inAppOverlays.forEach { overlay ->
            removeApplicationOverlay(overlay)
        }
        inAppOverlays.clear()
        removeGlobalOverlay()
    }

    /**
     * Detaches the view associated with input context from its parent if has been previously attached.
     * @param context Context detaching context
     */
    fun detach(context: Context) {
        removeGlobalOverlay()
        val contextIdentifier = getContextIdentifier(context, OverlayType.CURRENT_APPLICATION)
        inAppOverlays.firstOrNull { getContextIdentifier(it.context, OverlayType.CURRENT_APPLICATION) == contextIdentifier }?.let { overlay ->
            removeApplicationOverlay(overlay)
            inAppOverlays.remove(overlay)
        }
    }

    private fun removeGlobalOverlay() {
        globalOverlay ?: return
        kotlin.runCatching {
            windowManager!!.removeViewImmediate(globalOverlay!!)
            windowManager = null
        }
        globalOverlay = null
    }

    private fun removeApplicationOverlay(overlay: View) = with(overlay) {
        (parent as? ViewGroup)?.removeView(overlay)
        removeOnLayoutChangeListener(this@ViewOverlayAttacher)
    }

    private fun getSystemOverlayLayoutParams(): WindowManager.LayoutParams {
        val type: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE

        val windowManagerLayoutParams = WindowManager.LayoutParams(
            with(globalOverlay!!.context.getScreenSize()) { max(x, y) },
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        windowManagerLayoutParams.gravity = Gravity.TOP or Gravity.START
        windowManagerLayoutParams.title = "ViewOverlayAttacher"

        return windowManagerLayoutParams
    }

    private fun getContextIdentifier(context: Context, type: OverlayType): Int {
        return when (type) {
            OverlayType.GLOBAL -> context.applicationContext.hashCode()
            OverlayType.CURRENT_APPLICATION -> context.hashCode()
        }
    }

    /**
     * @suppress
     */
    override fun onLayoutChange(v: View?, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {
        windowManager?.updateViewLayout(v, v!!.layoutParams)
    }
}