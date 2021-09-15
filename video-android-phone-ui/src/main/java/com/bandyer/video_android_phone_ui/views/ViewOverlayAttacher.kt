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

package com.bandyer.video_android_phone_ui.views

import android.app.Activity
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout

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

    private var type: OverlayType? = null
    private val windowManager = view.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var attachId = 0

    /**
     * Attaches the view to the current application or over all applications with the given context with the OverlayType specified.
     * @param context Context
     * @param type OverlayType
     */
    fun attach(context: Context, type: OverlayType) {
        view.requestLayout()

        val contextIdentifier = getContextIdentifier(context, type)
        if (attachId == contextIdentifier) return
        detach()
        if (context !is Activity && type == OverlayType.CURRENT_APPLICATION) return
        attachId = contextIdentifier
        this.type = type
        when {
            this.type == OverlayType.GLOBAL -> {
                view.addOnLayoutChangeListener(this)
                runCatching { windowManager.addView(view, getSystemOverlayLayoutParams()) }
            }
            context is Activity -> (context.window.decorView as ViewGroup).addView(view, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
            else -> Unit
        }
    }

    /**
     * Detaches the view from its parent if has been previously attached.
     */
    fun detach() {
        if (attachId == 0) return
        if (type == OverlayType.GLOBAL) kotlin.runCatching { windowManager.removeViewImmediate(view) }
        else (view.parent as? ViewGroup)?.removeView(view)
        view.removeOnLayoutChangeListener(this)
        type = null
        attachId = 0
    }

    private fun getSystemOverlayLayoutParams(): WindowManager.LayoutParams {
        val type: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE

        val windowManagerLayoutParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                type,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT)
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
        windowManager.updateViewLayout(v, v!!.layoutParams)
    }
}