package com.kaleyra.video_common_ui.utils

import com.kaleyra.video.conference.Input
import com.kaleyra.video.conference.Inputs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

object InputsExtensions {

    fun Inputs.useBackCamera(coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)) {
        coroutineScope.launch {
            val input = availableInputs
                .map { inputs -> inputs.lastOrNull { it is Input.Video.Camera.Internal }}
                .filterIsInstance<Input.Video.Camera.Internal>()
                .first()

            if (input.currentLens.value.isRear) return@launch
            val newLens = input.lenses.firstOrNull { it.isRear } ?: return@launch
            input.setLens(newLens)
        }
    }
}