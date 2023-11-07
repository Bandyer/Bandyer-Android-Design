package com.kaleyra.collaboration_suite_core_ui.texttospeech

import com.kaleyra.collaboration_suite.conference.Call
import com.kaleyra.collaboration_suite_core_ui.CallUI
import com.kaleyra.collaboration_suite_core_ui.R
import com.kaleyra.video_utils.proximity_listener.ProximitySensor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile

internal class CallRecordingTextToSpeechNotifier(
    override val call: CallUI,
    override val proximitySensor: ProximitySensor,
    override val callTextToSpeech: CallTextToSpeech = CallTextToSpeech()
) : TextToSpeechNotifier {

    companion object {
        const val CALL_RECORDING_DEBOUNCE_MILLIS = 500L
    }

    private var currentJob: Job? = null

    override fun start(scope: CoroutineScope) {
        dispose()

        currentJob = call.recording
            .combine(call.state) { recording, callState -> recording to callState }
            .takeWhile { (_, callState) -> callState !is Call.State.Disconnected.Ended }
            // add a debounce to avoid the text to be reproduced when the call is ended and the app is in background
            .debounce(CALL_RECORDING_DEBOUNCE_MILLIS)
            .onEach { (recording, _)  ->
                if (!shouldNotify) return@onEach
                when (recording.type) {
                    Call.Recording.Type.OnDemand -> {
                        val text = context.getString(R.string.kaleyra_utterance_recording_call_may_be_recorded)
                        callTextToSpeech.speak(text)
                    }
                    Call.Recording.Type.OnConnect -> {
                        val text = context.getString(R.string.kaleyra_utterance_recording_call_will_be_recorded)
                        callTextToSpeech.speak(text)
                    }
                    else -> Unit
                }
            }
            .flatMapLatest { (recording, _) ->  recording.state }
            .dropWhile { it is Call.Recording.State.Stopped }
            .onEach { recordingState ->
                if (!shouldNotify) return@onEach
                val text = when (recordingState) {
                    is Call.Recording.State.Started -> context.getString(R.string.kaleyra_utterance_recording_started)
                    is Call.Recording.State.Stopped.Error -> context.getString(R.string.kaleyra_utterance_recording_failed)
                    is Call.Recording.State.Stopped -> context.getString(R.string.kaleyra_utterance_recording_stopped)
                }
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