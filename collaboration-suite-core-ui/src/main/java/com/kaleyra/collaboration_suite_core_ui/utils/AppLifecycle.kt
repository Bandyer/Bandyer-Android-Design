package com.kaleyra.collaboration_suite_core_ui.utils

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle.State
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal object AppLifecycle {

    private val _isAppInForeground by lazy { MutableStateFlow(ProcessLifecycleOwner.get().lifecycle.currentState == State.STARTED) }

    /**
     * A flow which notify if the application goes to foreground/background
     */
    val isInForeground: StateFlow<Boolean> by lazy { _isAppInForeground }

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner): Unit = let { _isAppInForeground.value = true }
            override fun onStop(owner: LifecycleOwner): Unit = let { _isAppInForeground.value = false }
        })
    }
}

