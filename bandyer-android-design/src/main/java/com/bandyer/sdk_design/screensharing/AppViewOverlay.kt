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

package com.bandyer.sdk_design.screensharing

import android.app.Activity
import android.app.AppOpsManager
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.view.View
import com.badoo.mobile.util.WeakHandler
import com.bandyer.sdk_design.extensions.canDrawOverlays
import com.bandyer.sdk_design.extensions.startAppOpsWatch
import com.bandyer.sdk_design.extensions.stopAppOpsWatch
import com.bandyer.sdk_design.views.ViewOverlayAttacher

/**
 * Represents a view that can is attached to the views of an app or attached to the system's window
 * if the device has permissions to do it.
 * @property view the View that will be placed upon app or system's window.
 * @property desiredType the request type of Overlay, by default is GLOBAL and fallbacks to CURRENT_APPLICATION
 */
class AppViewOverlay(val view: View, val desiredType: ViewOverlayAttacher.OverlayType = ViewOverlayAttacher.OverlayType.GLOBAL) {

    private val viewOverlayAttacher = ViewOverlayAttacher(view)

    private var application: Application? = null

    private var appOpsCallback: ((String, String) -> Unit)? = null

    private var initialized = false

    private val mainThreadHandler = WeakHandler(Looper.getMainLooper())

    /**
     * Shows the overlay view. If the context is an Activity the overlay will be attached to its decor view otherwise
     * if the context is an application context the view it will be attached to the system's window.
     * @param context Context used to attach the overlay view.
     */
    fun show(context: Context) {
        if (initialized) return
        initialized = true
        application = context.applicationContext as Application
        application!!.registerActivityLifecycleCallbacks(activityCallbacks)
        if (desiredType == ViewOverlayAttacher.OverlayType.GLOBAL) watchOverlayPermission(context)
        viewOverlayAttacher.attach(context, getOverlayType(context))
    }

    /**
     * Hides the screen share overlay.
     */
    fun hide() {
        application?.unregisterActivityLifecycleCallbacks(activityCallbacks)
        viewOverlayAttacher.detachAll()
        appOpsCallback?.let { application?.stopAppOpsWatch(it) }
        appOpsCallback = null
        initialized = false
        application = null
        mainThreadHandler.removeCallbacksAndMessages(null)
    }

    private fun getOverlayType(context: Context): ViewOverlayAttacher.OverlayType {
        return if (desiredType == ViewOverlayAttacher.OverlayType.GLOBAL && context.canDrawOverlays()) ViewOverlayAttacher.OverlayType.GLOBAL
        else ViewOverlayAttacher.OverlayType.CURRENT_APPLICATION
    }

    //////////////////////////////////// ACTIVITY CALLBACKS ////////////////////////////////////////////////////////////////////////

    private var activityCallbacks = object : Application.ActivityLifecycleCallbacks {

        override fun onActivityResumed(activity: Activity) {
            if (!initialized) return
            activity.window.decorView.post { viewOverlayAttacher.attach(activity, getOverlayType(activity)) }
        }

        override fun onActivityStopped(activity: Activity) = Unit
        override fun onActivityPaused(activity: Activity) = Unit
        override fun onActivityStarted(activity: Activity) = Unit
        override fun onActivityDestroyed(activity: Activity) = viewOverlayAttacher.detach(activity)
        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
    }

    //////////////////////////////////// OVERLAY PERMISSION ////////////////////////////////////////////////////////////////////////

    private fun watchOverlayPermission(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
        if (appOpsCallback != null) return
        val applicationContext = context.applicationContext
        val pckName = applicationContext.packageName
        appOpsCallback = callback@{ op, packageName ->
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW != op || packageName != pckName || !initialized) return@callback
            mainThreadHandler.post {
                hide()
                show(context)
            }
        }
        applicationContext.startAppOpsWatch(AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW, appOpsCallback!!)
    }
}