package com.kaleyra.collaboration_suite_core_ui.texttospeech

import com.kaleyra.collaboration_suite_core_ui.CallUI
import com.kaleyra.collaboration_suite_core_ui.R
import com.kaleyra.collaboration_suite_core_ui.mapper.StreamMapper.amIWaitingOthers
import com.kaleyra.video_utils.proximity_listener.ProximitySensor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach

internal class AwaitingParticipantsTextToSpeechNotifier(
    override val call: CallUI,
    override val proximitySensor: ProximitySensor,
    override val callTextToSpeech: CallTextToSpeech = CallTextToSpeech()
): TextToSpeechNotifier {

    companion object {
        const val AM_I_WAITING_FOR_OTHERS_DEBOUNCE_MILLIS = 2000L
    }

    private var currentJob: Job? = null

    override fun start(scope: CoroutineScope) {
        dispose()

        currentJob = flowOf(call)
            .amIWaitingOthers()
            .debounce(AM_I_WAITING_FOR_OTHERS_DEBOUNCE_MILLIS)
            .onEach { amIAwaiting ->
                if (!amIAwaiting || !shouldNotify) return@onEach
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