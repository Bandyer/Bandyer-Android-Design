package com.kaleyra.collaboration_suite_core_ui.texttospeech

import android.content.Context
import com.kaleyra.collaboration_suite.conference.Call
import com.kaleyra.collaboration_suite_core_ui.CallUI
import com.kaleyra.collaboration_suite_core_ui.R
import com.kaleyra.collaboration_suite_utils.ContextRetainer
import com.kaleyra.collaboration_suite_utils.proximity_listener.ProximitySensor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach

internal class CallRecordingTextToSpeechNotifier(
    override val call: CallUI,
    override val proximitySensor: ProximitySensor
) : TextToSpeechNotifier {

    private val context: Context
        get() = ContextRetainer.context

    private var currentJob: Job? = null

    override fun start(scope: CoroutineScope) {
        dispose()

        val textToSpeech = CallTextToSpeech()

        currentJob = call.recording
            .onEach { recording ->
                if (!shouldNotify()) return@onEach
                when (recording.type) {
                    Call.Recording.Type.OnDemand -> {
                        val text = context.getString(R.string.kaleyra_utterance_recording_call_may_be_recorded)
                        textToSpeech.speak(text)
                    }
                    Call.Recording.Type.OnConnect -> {
                        val text = context.getString(R.string.kaleyra_utterance_recording_call_will_be_recorded)
                        textToSpeech.speak(text)
                    }
                    else -> Unit
                }
            }
            .flatMapLatest { it.state }
            .dropWhile { it is Call.Recording.State.Stopped }
            .onEach { state ->
                if (!shouldNotify()) return@onEach
                val text = when (state) {
                    is Call.Recording.State.Started -> context.getString(R.string.kaleyra_utterance_recording_started)
                    is Call.Recording.State.Stopped.Error -> context.getString(R.string.kaleyra_utterance_recording_failed)
                    is Call.Recording.State.Stopped -> context.getString(R.string.kaleyra_utterance_recording_stopped)
                }
                textToSpeech.speak(text)
            }
            .onCompletion { textToSpeech.dispose(instantly = false) }
            .launchIn(scope)
    }

    override fun dispose() {
        currentJob?.cancel()
        currentJob = null
    }

}