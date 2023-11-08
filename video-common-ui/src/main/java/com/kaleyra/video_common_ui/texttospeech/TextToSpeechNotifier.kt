package com.kaleyra.video_common_ui.texttospeech

import android.content.Context
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.utils.AppLifecycle
import com.kaleyra.video_utils.ContextRetainer
import com.kaleyra.video_utils.proximity_listener.ProximitySensor
import kotlinx.coroutines.CoroutineScope

internal interface TextToSpeechNotifier {

    val call: CallUI

    val proximitySensor: ProximitySensor

    val callTextToSpeech: CallTextToSpeech

    val context: Context
        get() = ContextRetainer.context

    val shouldNotify: Boolean
        get() = !AppLifecycle.isInForeground.value || proximitySensor.isNear()

    fun start(scope: CoroutineScope)

    fun dispose()

}