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

import android.os.Build
import android.view.*
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentActivity
import com.bandyer.android_common.LifecycleEvents
import com.bandyer.android_common.LifecyleBinder
import com.bandyer.sdk_design.utils.systemviews.SystemViewLayoutObserver
import kotlin.math.max

internal class SystemViewControlsAware(val finished: () -> Unit) : SystemViewControlsAwareInstance, SystemViewLayoutObserver {

    private data class Inset(val left: Int, val top: Int, val right: Int, val bottom: Int)

    private var currentInset = Inset(0,0,0,0)
    private var currentCutOut = Inset(0,0,0,0)

    /**
     * Mapping of observers and requests to keep listening on global layout changes
     */
    private var systemUiObservers = mutableListOf<Pair<SystemViewLayoutObserver, Boolean>>()

    fun bind(activity: FragmentActivity): SystemViewControlsAware {
        LifecyleBinder.bind(activity, object : LifecycleEvents {
            override fun destroy() = dispose()
            override fun create() = Unit
            override fun pause() = Unit
            override fun resume() = Unit
            override fun start() = Unit
            override fun stop() = Unit
        })

        activity.window!!.decorView.apply {
            requestApplyInsetsWhenAttached()

            ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

                val newInsets = Inset(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                if (currentInset == newInsets) return@setOnApplyWindowInsetsListener WindowInsetsCompat.CONSUMED
                currentInset = newInsets

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    activity.window.decorView.rootWindowInsets.displayCutout?.let {
                        currentCutOut = Inset(it.safeInsetLeft, it.safeInsetTop, it.safeInsetRight, it.safeInsetBottom)
                    }
                }

                resetMargins()
                stopObserversListeningIfNeeded()

                return@setOnApplyWindowInsetsListener WindowInsetsCompat.CONSUMED
            }
        }

        return this
    }

    override fun addObserver(
        observer: SystemViewLayoutObserver,
        removeOnInsetChanged: Boolean
    ): SystemViewControlsAware {
        val addedObserver = systemUiObservers.firstOrNull { it.first == observer }?.first
        if (addedObserver != null) return this
        systemUiObservers.add(Pair(observer, removeOnInsetChanged))
        observer.onBottomInsetChanged(currentInset.bottom)
        observer.onTopInsetChanged(currentInset.top)
        observer.onLeftInsetChanged(currentInset.left)
        observer.onRightInsetChanged(currentInset.right)
        return this
    }

    override fun removeObserver(observer: SystemViewLayoutObserver): SystemViewControlsAware {
        systemUiObservers = systemUiObservers.filterNot { it.first == observer }.toMutableList()
        return this
    }

    override fun getOffsets() = resetMargins()

    private fun resetMargins() {
        onTopInsetChanged(max(currentCutOut.top, currentInset.top))
        onBottomInsetChanged(max(currentCutOut.bottom, currentInset.bottom))
        onLeftInsetChanged(max(currentCutOut.left, currentInset.left))
        onRightInsetChanged(max(currentCutOut.right, currentInset.right))
    }

    override fun onTopInsetChanged(pixels: Int) = systemUiObservers.forEach { it.first.onTopInsetChanged(pixels) }

    override fun onBottomInsetChanged(pixels: Int) = systemUiObservers.forEach { it.first.onBottomInsetChanged(pixels) }

    override fun onLeftInsetChanged(pixels: Int) = systemUiObservers.forEach { it.first.onLeftInsetChanged(pixels) }

    override fun onRightInsetChanged(pixels: Int) = systemUiObservers.forEach { it.first.onRightInsetChanged(pixels) }

    private fun stopObserversListeningIfNeeded() { systemUiObservers = systemUiObservers.filter { !it.second }.toMutableList() }

    private fun dispose() {
        systemUiObservers.clear()
        this@SystemViewControlsAware.finished()
    }

    private fun View.requestApplyInsetsWhenAttached() {
        if (isAttachedToWindow) {
            requestApplyInsets()
        } else {
            addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View) {
                    v.removeOnAttachStateChangeListener(this)
                    v.requestApplyInsets()
                }
                override fun onViewDetachedFromWindow(v: View) = Unit
            })
        }
    }
}