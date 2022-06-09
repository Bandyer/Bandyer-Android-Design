package com.kaleyra.collaboration_suite_core_ui.utils

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


internal object AppLifecycle {

    private val _isAppInForeground = MutableStateFlow(false)

    val isInForeground: StateFlow<Boolean> = _isAppInForeground

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) = let { _isAppInForeground.value = true }
            override fun onStop(owner: LifecycleOwner) = let { _isAppInForeground.value = false }
        })
    }
}

