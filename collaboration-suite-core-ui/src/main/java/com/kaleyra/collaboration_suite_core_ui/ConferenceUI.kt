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

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.kaleyra.collaboration_suite.conference.Call
import com.kaleyra.collaboration_suite.conference.Conference
import com.kaleyra.collaboration_suite_core_ui.utils.AppLifecycle
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ContextExtensions.isDND
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ContextExtensions.isSilent
import com.kaleyra.collaboration_suite_extension_audio.extensions.CollaborationAudioExtensions.disableAudioRouting
import com.kaleyra.collaboration_suite_extension_audio.extensions.CollaborationAudioExtensions.enableAudioRouting
import com.kaleyra.collaboration_suite_utils.ContextRetainer
import com.kaleyra.collaboration_suite_utils.logging.PriorityLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

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

    private var mappedCalls: List<CallUI> = listOf()

    /**
     * @suppress
     */

    override val call: SharedFlow<CallUI> = conference.call.map { getOrCreateCallUI(it) }.shareIn(callScope, SharingStarted.Eagerly, replay = 1)

    /**
     * @suppress
     */
    override val callHistory: SharedFlow<List<CallUI>> = conference.callHistory.map { it.map { getOrCreateCallUI(it) } }.shareIn(callScope, SharingStarted.Eagerly, replay = 1)


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

    /**
     * @suppress
     */
    override fun connect() = conference.connect()

    /**
     * @suppress
     */
    override fun disconnect(clearSavedData: Boolean) = conference.disconnect(clearSavedData)

    internal fun dispose() {
        disconnect()
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
    override fun create(url: String): Result<CallUI> = synchronized(this) {
        conference.create(url).map { createCallUI(it) }
    }

    /**
     * @suppress
     */
    override fun create(userIDs: List<String>, conf: (Conference.CreationOptions.() -> Unit)?): Result<CallUI> = synchronized(this) {
        conference.create(userIDs, conf).map {
            createCallUI(it)
        }
    }

    /**
     * Show the call ui
     */
    fun showCall() {
        if (!AppLifecycle.isInForeground.value) return
        UIProvider.showCall(callActivityClazz)
    }

    private fun internalShow(call: CallUI) {
        if (!canShowCallActivity(ContextRetainer.context, call)) return
        UIProvider.showCall(callActivityClazz)
    }

    private fun listenToCalls() {
        var serviceJob: Job? = null
        var currentCall: CallUI? = null
        val mutex = Mutex()
        call.onEach { call ->
            when {
                mutex.withLock { currentCall == null } -> {
                    if (call.state.value is Call.State.Disconnected.Ended || !withUI) return@onEach
                    mutex.withLock { currentCall = call }
                    serviceJob?.cancel()
                    serviceJob = callService(call, callScope) {
                        mutex.withLock { currentCall = null }
                    }
                    call.enableAudioRouting(withCallSounds = true, logger = logger, coroutineScope = callScope, isLink = call.isLink)
                    if (call.isLink) showOnAppResumed(call) else internalShow(call)
                }
                call.isLink                            -> showCannotJoinUrl()
            }
        }.onCompletion {
            with(ContextRetainer.context) { stopService(Intent(this, CallService::class.java)) }
        }.launchIn(callScope)
    }

    private fun callService(call: CallUI, scope: CoroutineScope, onServiceStopped: suspend () -> Unit): Job = with(ContextRetainer.context) {
        var isCallServiceStarted = false
        call.state
            .onEach { state ->
                when {
                    state is Call.State.Disconnected.Ended -> {
                        stopService(Intent(this, CallService::class.java))
                        onServiceStopped()
                    }
                    !isCallServiceStarted                  -> {
                        val intent = Intent(this, CallService::class.java)
                        intent.putExtra(CallService.CALL_ACTIVITY_CLASS, callActivityClazz)
                        startService(intent)
                        isCallServiceStarted = true
                    }
                }
            }
            .launchIn(scope)
    }

    private fun canShowCallActivity(context: Context, call: CallUI): Boolean {
        val participants = call.participants.value
        val creator = participants.creator()
        val isOutgoing = creator == participants.me
        return AppLifecycle.isInForeground.value &&
                (!context.isDND() || (context.isDND() && isOutgoing)) &&
                (!context.isSilent() || (context.isSilent() && (isOutgoing || call.isLink)))
    }

    private fun showCannotJoinUrl() {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(ContextRetainer.context, R.string.kaleyra_call_join_url_already_in_call_error, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showOnAppResumed(call: CallUI): Unit = let { AppLifecycle.isInForeground.dropWhile { !it }.take(1).onEach { internalShow(call) }.launchIn(callScope) }

    private fun getOrCreateCallUI(call: Call): CallUI = synchronized(this) { mappedCalls.firstOrNull { it.id == call.id } ?: createCallUI(call) }

    private fun createCallUI(call: Call): CallUI = CallUI(call = call, actions = MutableStateFlow(callActions)).apply { mappedCalls = mappedCalls + this }
}