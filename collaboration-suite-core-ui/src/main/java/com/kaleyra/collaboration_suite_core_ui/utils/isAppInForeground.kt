package com.kaleyra.collaboration_suite_core_ui.utils

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

internal object AppLifecycle {

    private val _isAppInForeground = MutableSharedFlow<Boolean>(replay = 1, extraBufferCapacity = 1)

    /**
     * A flow which notify if the application goes to foreground/background
     */
    val isInForeground: SharedFlow<Boolean> = _isAppInForeground

    /**
     * True if the app is in foreground, false otherwise
     *
     * @return Boolean
     */
    fun isInForeground(): Boolean = isInForeground.replayCache.firstOrNull() ?: false

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner): Unit = let { MainScope().launch { _isAppInForeground.emit(true) } }
            override fun onStop(owner: LifecycleOwner): Unit = let {  MainScope().launch { _isAppInForeground.emit(false) } }
        })
    }
}

