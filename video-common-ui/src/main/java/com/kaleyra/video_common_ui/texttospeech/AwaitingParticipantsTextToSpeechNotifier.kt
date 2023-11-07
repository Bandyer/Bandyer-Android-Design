package com.kaleyra.video_common_ui.texttospeech

import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.R
import com.kaleyra.video_common_ui.mapper.StreamMapper.amIWaitingOthers
import com.kaleyra.video_utils.proximity_listener.ProximitySensor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach

internal class AwaitingParticipantsTextToSpeechNotifier(
    override val call: CallUI,
    override val proximitySensor: ProximitySensor,
    override val callTextToSpeech: CallTextToSpeech = CallTextToSpeech()
): TextToSpeechNotifier {

    private var currentJob: Job? = null

    override fun start(scope: CoroutineScope) {
        dispose()

        currentJob = flowOf(call)
            .amIWaitingOthers()
            .onEach {
                if (!shouldNotify) return@onEach
                val text = context.getString(R.string.kaleyra_call_waiting_for_other_participants)
                callTextToSpeech.speak(text)
            }
            .onCompletion { callTextToSpeech.dispose(instantly = false) }
            .launchIn(scope)
    }

    override fun dispose() {
        currentJob?.cancel()
        currentJob = null
    }

}