package com.kaleyra.collaboration_suite_core_ui.texttospeech

import android.content.Context
import com.kaleyra.collaboration_suite_core_ui.R
import com.kaleyra.collaboration_suite.conference.Call
import com.kaleyra.collaboration_suite.conference.Input
import com.kaleyra.collaboration_suite_core_ui.CallUI
import com.kaleyra.collaboration_suite_core_ui.utils.FlowUtils.flatMapLatestNotNull
import com.kaleyra.video_utils.ContextRetainer
import com.kaleyra.video_utils.proximity_listener.ProximitySensor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach

internal class CallParticipantMutedTextToSpeechNotifier(
    override val call: CallUI,
    override val proximitySensor: ProximitySensor
) : TextToSpeechNotifier {

    private val context: Context
        get() = ContextRetainer.context

    private var currentJob: Job? = null

    override fun start(scope: CoroutineScope) {
        dispose()

        val textToSpeech = CallTextToSpeech()
        currentJob = call.toMuteEvents()
            .onEach {
                if (!shouldNotify()) return@onEach
                val text = context.getString(R.string.kaleyra_call_participant_utterance_muted_by_admin)
                textToSpeech.speak(text)
            }
            .onCompletion { textToSpeech.dispose(instantly = false) }
            .launchIn(scope)
    }

    override fun dispose() {
        currentJob?.cancel()
        currentJob = null
    }

    private fun Call.toMuteEvents(): Flow<Input.Audio.Event.Request.Mute> {
        return participants
            .flatMapLatestNotNull { it.me?.streams }
            .map { streams ->
                streams.firstOrNull { stream ->
                    stream.audio.firstOrNull { it != null } != null
                }
            }
            .filterNotNull()
            .flatMapLatest { it.audio }
            .filterNotNull()
            .flatMapLatest { it.events }
            .filterIsInstance()
    }


}
