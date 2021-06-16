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
import android.os.Build
import android.view.*
import androidx.core.graphics.Insets
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
    private var hasChangedConfiguration = false
    private var wasInPiP = false
    private var currentSystemBarsInsets: Insets? = null
    /**
     * Mapping of observers and requests to keep listening on global layout channges
     */
    private var systemUiObservers = mutableListOf<Pair<SystemViewLayoutObserver, Boolean>>()

    @SuppressLint("NewApi")
    fun bind(activity: FragmentActivity): SystemViewControlsAware {
        context = WeakReference(activity)

        LifecyleBinder.bind(activity, object : LifecycleEvents {
            override fun destroy() = dispose()
            override fun create() = Unit
            override fun pause() = Unit
            override fun resume() = resetMargins()
            override fun start() = Unit
            override fun stop() = Unit
        })

        val decorView = activity.window!!.decorView

        decorView.post {
            activity.registerComponentCallbacks(this)
            decorView.addOnLayoutChangeListener(this)
            resetMargins()
        }

        decorView.setOnSystemUiVisibilityChangeListener {
            decorView.post {
                resetMargins()
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(decorView) { v, insets ->
            val context = context?.get() ?: return@setOnApplyWindowInsetsListener WindowInsetsCompat.CONSUMED
            val isInPiP = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && context.isInPictureInPictureMode
            val systemBarInsets = getSystemBarsInsets(decorView)

            val hasInsetsChanged = systemBarInsets != currentSystemBarsInsets
            currentSystemBarsInsets = systemBarInsets

            val isChangingPiPConfiguration = wasInPiP != isInPiP
            wasInPiP = isInPiP


            if (isChangingPiPConfiguration || !hasInsetsChanged)
                return@setOnApplyWindowInsetsListener WindowInsetsCompat.CONSUMED

            v.post {
                resetMargins()
            }

            return@setOnApplyWindowInsetsListener WindowInsetsCompat.CONSUMED
        }

        return this
    }

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

    override fun getOffsets() = resetMargins()

    override fun onLayoutChange(v: View?, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {
        if (!hasChangedConfiguration && (oldRight == right || oldLeft == left || oldTop == top || oldBottom == bottom)) return
        hasChangedConfiguration = false
        resetMargins()
    }

    private fun resetMargins() {
        val context = context?.get() ?: return
        val decorView = context.window.decorView ?: return

        if (decorView.width == 0 || decorView.height == 0) return

        if (context.checkIsInMultiWindowMode() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && decorView.elevation > 0.0f) {
            onTopInsetChanged(0)
            onBottomInsetChanged(0)
            onLeftInsetChanged(0)
            onRightInsetChanged(0)
            stopObserversListeningIfNeeded()
            return
        }

        val decorViewHeight = decorView.height
        val decorViewWidth = decorView.width

        if (!context.checkIsInMultiWindowMode() && (isPortrait() && decorViewWidth > decorViewHeight || !isPortrait() && decorViewHeight > decorViewWidth)) {
            decorView.post {
                resetMargins()
            }
            return
        }

        if (decorViewHeight == 0 || decorViewWidth == 0) return

        val currentInsets = getSystemBarsInsets(decorView) ?: return

        val bottomInset = currentInsets.bottom
        val topInset = currentInsets.top
        val leftInset = currentInsets.left
        val rightInset = currentInsets.right

        if (rightInset >= decorViewWidth || leftInset >= decorViewWidth || bottomInset >= decorViewHeight || topInset >= decorViewHeight) return

        onTopInsetChanged(abs(topInset))
        onBottomInsetChanged(abs(bottomInset))
        onLeftInsetChanged(abs(leftInset))
        onRightInsetChanged(abs(rightInset))

        stopObserversListeningIfNeeded()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        hasChangedConfiguration = true
        getOffsets()
    }

    override fun onTopInsetChanged(pixels: Int) = systemUiObservers.forEach { it.first.onTopInsetChanged(pixels) }

    override fun onBottomInsetChanged(pixels: Int) = systemUiObservers.forEach { it.first.onBottomInsetChanged(pixels) }

    override fun onLeftInsetChanged(pixels: Int) = systemUiObservers.forEach { it.first.onLeftInsetChanged(pixels) }

    override fun onRightInsetChanged(pixels: Int) = systemUiObservers.forEach { it.first.onRightInsetChanged(pixels) }

    private fun stopObserversListeningIfNeeded() {
        systemUiObservers = systemUiObservers.filter { !it.second }.toMutableList()
    }

    override fun onLowMemory() = Unit

    private fun dispose() {
        systemUiObservers.clear()
        val context = context?.get() ?: return
        context.window.decorView.removeOnLayoutChangeListener(this)
        context.unregisterComponentCallbacks(this)
        this@SystemViewControlsAware.finished()
    }

    private fun isPortrait() = context?.get()?.resources?.configuration?.orientation == Configuration.ORIENTATION_PORTRAIT

    private fun getSystemBarsInsets(view: View): Insets? = ViewCompat.getRootWindowInsets(view)?.getInsets(WindowInsetsCompat.Type.systemBars())
}