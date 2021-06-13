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

package com.bandyer.sdk_design.utils.systemviews.implementation

import android.annotation.SuppressLint
import android.content.ComponentCallbacks
import android.content.res.Configuration
import android.graphics.Rect
import android.os.Build
import android.view.*
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentActivity
import com.bandyer.android_common.LifecycleEvents
import com.bandyer.android_common.LifecyleBinder
import com.bandyer.sdk_design.extensions.checkIsInMultiWindowMode
import com.bandyer.sdk_design.utils.systemviews.SystemViewLayoutObserver
import java.lang.ref.WeakReference
import kotlin.math.abs

internal class SystemViewControlsAware(val finished: () -> Unit) : SystemViewControlsAwareInstance, SystemViewLayoutObserver, ComponentCallbacks, View.OnLayoutChangeListener {
    private var context: WeakReference<FragmentActivity>? = null
    private var isPortrait = true
    private var hasChangedConfiguration = false

    /**
     * Mapping of observers and requests to keep listening on global layout channges
     */
    private var systemUiObservers = mutableListOf<Pair<SystemViewLayoutObserver, Boolean>>()

    private var window: Window? = null

    @SuppressLint("NewApi")
    fun bind(activity: FragmentActivity): SystemViewControlsAware {
        context = WeakReference(activity)

        isPortrait = activity.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT

        LifecyleBinder.bind(activity, object : LifecycleEvents {
            override fun destroy() = dispose()
            override fun create() = Unit
            override fun pause() = Unit
            override fun resume() = resetMargins()
            override fun start() = Unit
            override fun stop() = Unit
        })

        this.window = activity.window

        window!!.decorView.post {
            val context = context?.get() ?: return@post
            context.registerComponentCallbacks(this)
            window?.decorView?.addOnLayoutChangeListener(this)
            resetMargins()
        }

        window!!.decorView.setOnSystemUiVisibilityChangeListener {
            window?.decorView?.post { resetMargins() }
        }

        val layout = (window!!.decorView as ViewGroup).getChildAt(0)
        ViewCompat.setOnApplyWindowInsetsListener(layout) { v, insets ->
            val isInPiP = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) && context?.get()?.isInPictureInPictureMode == true
            val rootInsets = v.rootView.rootWindowInsets
            val hasInsetsChanged = WindowInsetsCompat.toWindowInsetsCompat(rootInsets).getInsets(WindowInsetsCompat.Type.systemBars()) != oldInsets?.getInsets(WindowInsetsCompat.Type.systemBars())
            val isChangingPiPConfiguration = wasInPiP != isInPiP
            wasInPiP = isInPiP
            oldInsets = WindowInsetsCompat.toWindowInsetsCompat(rootInsets)

            if (isChangingPiPConfiguration || !hasInsetsChanged)
                return@setOnApplyWindowInsetsListener WindowInsetsCompat.CONSUMED

            v.post { resetMargins() }
            return@setOnApplyWindowInsetsListener WindowInsetsCompat.CONSUMED
        }

        return this
    }

    private var wasInPiP = false

    private var oldInsets: WindowInsetsCompat? = null

    override fun addObserver(observer: SystemViewLayoutObserver, removeOnInsetChanged: Boolean): SystemViewControlsAware {
        val addedObserver = systemUiObservers.firstOrNull { it.first == observer }?.first
        if (addedObserver != null) return this
        systemUiObservers.add(Pair(observer, removeOnInsetChanged))
        resetMargins()
        return this
    }


    override fun removeObserver(observer: SystemViewLayoutObserver): SystemViewControlsAware {
        systemUiObservers = systemUiObservers.filterNot { it.first == observer }.toMutableList()
        return this
    }

    override fun getOffsets() {
        resetMargins()
    }

    override fun onLayoutChange(v: View?, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {
        if (!hasChangedConfiguration && (oldRight == right || oldLeft == left || oldTop == top || oldBottom == bottom)) return
        hasChangedConfiguration = false
        resetMargins()
    }

    private fun resetMargins() {
        val context = context?.get() ?: return
        window ?: return
        val decorView = window!!.decorView as ViewGroup
        if (decorView.width == 0 || decorView.height == 0) return

        if (context.checkIsInMultiWindowMode() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && decorView.elevation > 0.0f) {
            onTopInsetChanged(0)
            onBottomInsetChanged(0)
            onLeftInsetChanged(0)
            onRightInsetChanged(0)
            stopObserversListeningIfNeeded()
            return
        }

        val rect = Rect()
        decorView.getWindowVisibleDisplayFrame(rect)
        val height = decorView.height
        val width = decorView.width

        if (height == 0 || width == 0) return

        val currentInsets = ViewCompat.getRootWindowInsets(decorView)?.getInsets(WindowInsetsCompat.Type.systemBars())

        val bottomMargin = oldInsets?.displayCutout?.safeInsetBottom?.takeIf { it > 0 } ?: currentInsets?.bottom ?: (height - rect.bottom).takeIf { it >= 0 } ?: 0
        val topMargin = oldInsets?.displayCutout?.safeInsetTop?.takeIf { it > 0 } ?: currentInsets?.top ?: rect.top.takeIf { it >= 0 } ?: 0
        val leftMargin = oldInsets?.displayCutout?.safeInsetLeft?.takeIf { it > 0 } ?: currentInsets?.left ?: rect.left.takeIf { it >= 0 } ?: 0
        val rightMargin = oldInsets?.displayCutout?.safeInsetRight?.takeIf { it > 0 } ?: currentInsets?.right ?: (width - rect.right).takeIf { it >= 0 } ?: 0

        if (rightMargin >= width || leftMargin >= width || bottomMargin >= height || topMargin >= height) return

        onTopInsetChanged(abs(topMargin))
        onBottomInsetChanged(abs(bottomMargin))
        onLeftInsetChanged(abs(leftMargin))
        onRightInsetChanged(abs(rightMargin))

        stopObserversListeningIfNeeded()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        isPortrait = newConfig.orientation == Configuration.ORIENTATION_PORTRAIT
        hasChangedConfiguration = true
        getOffsets()
    }

    override fun onTopInsetChanged(pixels: Int) {
        systemUiObservers.forEach { it.first.onTopInsetChanged(pixels) }
    }

    override fun onBottomInsetChanged(pixels: Int) {
        systemUiObservers.forEach { it.first.onBottomInsetChanged(pixels) }
    }

    override fun onLeftInsetChanged(pixels: Int) {
        systemUiObservers.forEach { it.first.onLeftInsetChanged(pixels) }
    }

    override fun onRightInsetChanged(pixels: Int) {
        systemUiObservers.forEach { it.first.onRightInsetChanged(pixels) }
    }

    private fun stopObserversListeningIfNeeded() {
        systemUiObservers = systemUiObservers.filter { !it.second }.toMutableList()
    }

    override fun onLowMemory() = Unit

    private fun dispose() {
        systemUiObservers.clear()
        val context = context?.get() ?: return
        window?.decorView?.removeOnLayoutChangeListener(this)
        context.unregisterComponentCallbacks(this)
        this@SystemViewControlsAware.finished()
    }
}