/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.video_common_ui.texttospeech

import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.R
import com.kaleyra.video_common_ui.mapper.StreamMapper.amIWaitingOthers
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