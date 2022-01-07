package com.bandyer.video_android_glass_ui.utils.extensions

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope

internal object LifecycleOwnerExtensions {
    /**
     * Runs the block of code in a coroutine when the lifecycle is at least STARTED.
     * The coroutine will be cancelled when the ON_STOP event happens and will
     * restart executing if the lifecycle receives the ON_START event again.
     * @receiver LifecycleCoroutineScope
     */
    fun LifecycleOwner.repeatOnStarted(code: suspend CoroutineScope.() -> Unit) =
        lifecycleScope.launchWhenStarted { this@repeatOnStarted.repeatOnLifecycle(Lifecycle.State.STARTED) { code.invoke(this) } }
}