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

import android.graphics.Rect
import android.view.*
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnAttach
import androidx.fragment.app.FragmentActivity
import com.bandyer.android_common.LifecycleEvents
import com.bandyer.android_common.LifecyleBinder
import com.bandyer.sdk_design.utils.systemviews.SystemViewLayoutObserver

internal class SystemViewControlsAware(val finished: () -> Unit) : SystemViewControlsAwareInstance {

    /**
     * Mapping of observers and requests to keep listening on global layout changes
     */
    private var systemUiObservers = mutableListOf<Pair<SystemViewLayoutObserver, Boolean>>()

    private var currentInset = Insets.of(Rect())

    fun bind(activity: FragmentActivity): SystemViewControlsAware = apply {

        LifecyleBinder.bind(activity, object : LifecycleEvents {
            override fun destroy() = dispose()
            override fun create() = Unit
            override fun pause() = Unit
            override fun resume() = Unit
            override fun start() = Unit
            override fun stop() = Unit
        })

        activity.window!!.decorView.apply {
            doOnAttach { requestApplyInsets() }

            ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->
                val newInsets = with(insets) {
                    Insets.max(getInsets(WindowInsetsCompat.Type.systemBars()), getInsets(WindowInsetsCompat.Type.displayCutout()))
                }

                if (currentInset == newInsets) return@setOnApplyWindowInsetsListener WindowInsetsCompat.CONSUMED

                currentInset = newInsets

                resetMargins()
                stopObserversListeningIfNeeded()

                return@setOnApplyWindowInsetsListener WindowInsetsCompat.CONSUMED
            }
        }
    }

    override fun addObserver(
        observer: SystemViewLayoutObserver,
        removeOnInsetChanged: Boolean
    ): SystemViewControlsAware {
        val addedObserver = systemUiObservers.firstOrNull { it.first == observer }?.first
        if (addedObserver != null) return this
        systemUiObservers.add(Pair(observer, removeOnInsetChanged))
        notifyObserver(observer)
        stopObserversListeningIfNeeded()
        return this
    }

    override fun removeObserver(observer: SystemViewLayoutObserver): SystemViewControlsAware = apply {
        systemUiObservers = systemUiObservers.filterNot { it.first == observer }.toMutableList()
    }

    override fun getOffsets() = resetMargins()

    private fun resetMargins() {
        systemUiObservers.forEach {
            notifyObserver(it.first)
        }
        stopObserversListeningIfNeeded()
    }

    private fun notifyObserver(observer: SystemViewLayoutObserver) = with(observer) {
        onTopInsetChanged(currentInset.top)
        onLeftInsetChanged(currentInset.left)
        onRightInsetChanged(currentInset.right)
        onBottomInsetChanged(currentInset.bottom)
    }

    private fun stopObserversListeningIfNeeded() {
        systemUiObservers = systemUiObservers.filter { !it.second }.toMutableList()
    }

    private fun dispose() {
        systemUiObservers.clear()
        this@SystemViewControlsAware.finished()
    }
}