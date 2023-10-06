package com.kaleyra.collaboration_suite_core_ui.texttospeech

import com.kaleyra.collaboration_suite_core_ui.CallUI
import com.kaleyra.collaboration_suite_core_ui.utils.AppLifecycle
import com.kaleyra.video_utils.proximity_listener.ProximitySensor
import kotlinx.coroutines.CoroutineScope

interface TextToSpeechNotifier {

    val call: CallUI

    val proximitySensor: ProximitySensor

    fun start(scope: CoroutineScope)

    fun dispose()

    fun shouldNotify() = !AppLifecycle.isInForeground.value || proximitySensor.isNear()
}