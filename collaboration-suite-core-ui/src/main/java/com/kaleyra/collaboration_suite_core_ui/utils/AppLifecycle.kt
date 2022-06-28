package com.kaleyra.collaboration_suite_core_ui.utils

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

internal object AppLifecycle {

    private val _isAppInForeground = MutableStateFlow(false)

    /**
     * A flow which notify if the application goes to foreground/background
     */
    val isInForeground: StateFlow<Boolean> = _isAppInForeground

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner): Unit = let { MainScope().launch { _isAppInForeground.value = true} }
            override fun onStop(owner: LifecycleOwner): Unit = let {  MainScope().launch { _isAppInForeground.value = false } }
        })
    }
}

