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

import com.kaleyra.collaboration_suite.AccessTokenProvider
import com.kaleyra.collaboration_suite.Collaboration
import com.kaleyra.collaboration_suite.State
import com.kaleyra.collaboration_suite.Synchronization
import com.kaleyra.collaboration_suite.User
import com.kaleyra.collaboration_suite.conference.Call
import com.kaleyra.collaboration_suite_core_ui.notification.NotificationManager
import com.kaleyra.collaboration_suite_core_ui.utils.AppLifecycle
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * The collaboration UI connector
 *
 * @property collaboration The collaboration UI
 * @property scope The coroutine scope
 * @constructor
 */
internal class CollaborationUIConnector(val collaboration: Collaboration, private val parentScope: CoroutineScope) {

    private enum class Action {
        RESUME,
        PAUSE
    }

    private var lastAction: Action? = null

    private var endedCallIds = mutableSetOf<String>()

    private var scope = CoroutineScope(SupervisorJob(parentScope.coroutineContext[Job]) + Dispatchers.IO)

    init {
        syncWithAppLifecycle(parentScope)
    }


    /**
     * Connect the collaboration
     */
    fun connect(userId: String, accessTokenProvider: AccessTokenProvider): Deferred<User> = collaboration.connect(userId, accessTokenProvider).apply {
        resume()
    }

    /**
     * Disconnect the collaboration
     */
    fun disconnect(clearSavedData: Boolean = false) {
        collaboration.disconnect(clearSavedData)
        scope.coroutineContext.cancelChildren()
        if (clearSavedData) NotificationManager.cancelAll()
    }

    private fun pause() {
        scope.coroutineContext.cancelChildren()
    }

    /**
     * Connect the collaboration
     */
    fun connect(accessLink: String): Deferred<User> = collaboration.connect(accessLink).apply {
        resume()
    }

    private fun resume() {
        scope.coroutineContext.cancelChildren()
        syncWithCallState(scope)
        syncWithChatMessages(scope)
    }

    private fun syncWithAppLifecycle(scope: CoroutineScope) {
        AppLifecycle.isInForeground
            .dropWhile { !it }
            .onEach { isInForeground ->
                if (isInForeground) performAction(Action.RESUME)
                else {
                    val currentCall = collaboration.conference.call.replayCache.firstOrNull()
                    val isInCall = currentCall != null && currentCall.state.value !is Call.State.Disconnected.Ended
                    if (!isInCall) performAction(Action.PAUSE)
                }
            }
            .launchIn(scope)
    }

    private fun syncWithCallState(scope: CoroutineScope) {
        val conferenceState = collaboration.conference.state
        val callState = collaboration.conference.call.flatMapLatest { it.state }
        scope.launch {
            combine(conferenceState, callState, AppLifecycle.isInForeground) { conferenceState, callState, isInForeground ->
                conferenceState is State.Connected && callState is Call.State.Disconnected.Ended && !isInForeground
            }.collectLatest {
                if (!it) return@collectLatest

                val endedCallId = collaboration.conference.call.replayCache.firstOrNull()?.id
                if (endedCallId in endedCallIds) return@collectLatest
                endedCallId?.let { endedCallIds.add(it) }

                delay(300)
                performAction(Action.PAUSE)
            }
        }
    }

    private fun syncWithChatMessages(scope: CoroutineScope) {
        collaboration.synchronization
            .dropWhile { it !is Synchronization.Active.Completed }
            .onEach {
                val conference = collaboration.conference
                val call = conference.call.replayCache.firstOrNull()
                if (AppLifecycle.isInForeground.value ||
                    (call != null && call.state.value !is Call.State.Disconnected.Ended) ||
                    conference.state.value !is State.Connected
                ) return@onEach
                performAction(Action.PAUSE)
            }.launchIn(scope)
    }

    private fun performAction(action: Action) {
        synchronized(this) {
            when (action) {
                Action.RESUME -> resume()
                Action.PAUSE  -> pause()
            }
            lastAction = action
        }
    }
}