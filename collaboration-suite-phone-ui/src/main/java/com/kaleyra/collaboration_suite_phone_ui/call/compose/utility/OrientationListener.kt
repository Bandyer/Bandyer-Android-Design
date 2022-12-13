package com.kaleyra.collaboration_suite_phone_ui.call.compose.utility

import android.content.Context
import android.view.OrientationEventListener
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

internal class OrientationListener(context: Context) : OrientationEventListener(context) {

    private val _orientation: MutableSharedFlow<Int> = MutableSharedFlow(replay = 1, extraBufferCapacity = 1)

    val orientation: Flow<Int>
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