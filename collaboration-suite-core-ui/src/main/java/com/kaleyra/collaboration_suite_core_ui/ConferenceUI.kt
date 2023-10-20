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

package com.kaleyra.collaboration_suite_core_ui

import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.kaleyra.collaboration_suite.conference.Call
import com.kaleyra.collaboration_suite.conference.Conference
import com.kaleyra.collaboration_suite_core_ui.CallUI.Companion.toUI
import com.kaleyra.collaboration_suite_core_ui.utils.CallExtensions.shouldShowAsActivity
import com.kaleyra.collaboration_suite_core_ui.utils.CallExtensions.showOnAppResumed
import com.kaleyra.collaboration_suite_extension_audio.extensions.CollaborationAudioExtensions.disableAudioRouting
import com.kaleyra.collaboration_suite_extension_audio.extensions.CollaborationAudioExtensions.enableAudioRouting
import com.kaleyra.video_utils.ContextRetainer
import com.kaleyra.video_utils.logging.PriorityLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn

/**
 * Conference UI
 *
 * @property conference The Conference delegate
 * @property callActivityClazz The call activity Class<*>
 * @property logger The PriorityLogger
 * @constructor
 */
class ConferenceUI(
    private val conference: Conference,
    private val callActivityClazz: Class<*>,
    private val logger: PriorityLogger? = null,
) : Conference by conference {

    private val callScope = CoroutineScope(Dispatchers.IO)

    private var callUIMap: HashMap<String, CallUI> = HashMap()

    /**
     * @suppress
     */
    override val call: SharedFlow<CallUI> = conference.call.map { getOrCreateCallUI(it) }.shareIn(callScope, SharingStarted.Eagerly, replay = 1)

    /**
     * @suppress
     */
    override val callHistory: SharedFlow<List<CallUI>> = conference.callHistory.map { calls -> calls.map { getOrCreateCallUI(it) } }.shareIn(callScope, SharingStarted.Eagerly, replay = 1)

    /**
     * WithUI flag, set to true to automatically show the call ui on a new call, false otherwise
     */
    var withUI = true

    /**
     * The call actions that will be set on every call
     */
    var callActions: Set<CallUI.Action> = CallUI.Action.default

    init {
        listenToCalls()
    }

    internal fun dispose() {
        disableAudioRouting(logger)
        callScope.cancel()
    }
    /**
     * Call
     *
     * @param userIDs to be called
     * @param options creation options
     */
    fun call(userIDs: List<String>, options: (Conference.CreationOptions.() -> Unit)? = null): Result<CallUI> = create(userIDs, options).onSuccess { it.connect() }

    /**
     * Join an url
     *
     * @param url to join
     */
    fun joinUrl(url: String): Result<CallUI> = create(url).onSuccess { it.connect() }

    /**
     * @suppress
     */
    override fun create(url: String): Result<CallUI> =
        conference.create(url).map { getOrCreateCallUI(it) }

    /**
     * @suppress
     */
    override fun create(userIDs: List<String>, conf: (Conference.CreationOptions.() -> Unit)?): Result<CallUI> =
        conference.create(userIDs, conf).map { getOrCreateCallUI(it) }

    private fun listenToCalls() {
        var currentCall: CallUI? = null
        call
            .onEach { call ->
                if (call.state.value is Call.State.Disconnected.Ended) return@onEach

                if (currentCall != call && call.isLink) {
                    showCannotJoinUrl()
                    return@onEach
                }

                CallService.start()
                call.enableAudioRouting(withCallSounds = true, logger = logger, coroutineScope = callScope, isLink = call.isLink)
                when {
                    !withUI -> Unit
                    call.isLink -> call.showOnAppResumed(callScope)
                    call.shouldShowAsActivity() -> call.show()
                }
                currentCall = call
            }
            .onCompletion { CallService.stop() }
            .launchIn(callScope)
    }

    private fun showCannotJoinUrl() = Handler(Looper.getMainLooper()).post {
        Toast.makeText(ContextRetainer.context, R.string.kaleyra_call_join_url_already_in_call_error, Toast.LENGTH_SHORT).show()
    }

    private fun getOrCreateCallUI(call: Call): CallUI = synchronized(this) {
        callUIMap[call.id] ?: call
            .toUI(activityClazz = callActivityClazz, actions = MutableStateFlow(callActions))
            .apply { callUIMap[id] = this }
    }

}