package com.kaleyra.video_common_ui

import com.kaleyra.video.conference.Input
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take

interface DefaultCameraDelegate {

    fun setBackCameraAsDefault(call: CallUI, scope: CoroutineScope) {
        call.inputs.availableInputs
            .map { inputs -> inputs.lastOrNull { it is Input.Video.Camera.Internal }}
            .filterIsInstance<Input.Video.Camera.Internal>()
            .take(1)
            .onEach { input ->
                if (input.currentLens.value.isRear) return@onEach
                val newLens = input.lenses.firstOrNull { it.isRear } ?: return@onEach
                input.setLens(newLens)
            }
            .launchIn(scope)
    }
}