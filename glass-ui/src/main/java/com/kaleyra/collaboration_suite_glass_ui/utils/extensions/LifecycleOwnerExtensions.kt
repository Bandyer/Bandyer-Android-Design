/*
 * Copyright 2022 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.collaboration_suite_glass_ui.utils.extensions

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal object LifecycleOwnerExtensions {
    /**
     * Runs the block of code in a coroutine when the lifecycle is at least STARTED.
     * The coroutine will be cancelled when the ON_STOP event happens and will
     * restart executing if the lifecycle receives the ON_RESUME event again.
     * @receiver LifecycleCoroutineScope
     */
    fun LifecycleOwner.repeatOnStarted(code: suspend CoroutineScope.() -> Unit) =
        lifecycleScope.launch { this@repeatOnStarted.repeatOnLifecycle(Lifecycle.State.STARTED) { code.invoke(this) } }
}