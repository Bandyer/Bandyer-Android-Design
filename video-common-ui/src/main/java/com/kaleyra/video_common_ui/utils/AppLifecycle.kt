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

package com.kaleyra.video_common_ui.utils

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle.State
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

internal object AppLifecycle {

    private val _isAppInForeground by lazy { MutableStateFlow(ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(State.STARTED)) }

    /**
     * A flow which notify if the application goes to foreground/background
     */
    val isInForeground: StateFlow<Boolean> by lazy { _isAppInForeground }

    init {
        MainScope().launch {
            ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onStart(owner: LifecycleOwner): Unit = let { _isAppInForeground.value = true }
                override fun onStop(owner: LifecycleOwner): Unit = let { _isAppInForeground.value = false }
            })
        }
    }
}

