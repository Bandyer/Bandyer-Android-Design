package com.kaleyra.collaboration_suite_phone_ui.call.compose.utils

import android.content.Context
import android.view.OrientationEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal class OrientationListener(context: Context) : OrientationEventListener(context) {

    private val _orientation: MutableStateFlow<Int> = MutableStateFlow(0)

    val orientation: StateFlow<Int>
        get() = _orientation

    override fun onOrientationChanged(orientation: Int) {
        val degrees = when (orientation) {
            in 46..134 -> 90
            in 136..224 -> 180
            in 226..314 -> 270
            else -> 0
        }
        _orientation.tryEmit(degrees)
    }

}