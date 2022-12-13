package com.kaleyra.collaboration_suite_core_ui

import com.kaleyra.collaboration_suite.Collaboration
import com.kaleyra.collaboration_suite.chatbox.ChatBox
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite.phonebox.PhoneBox
import com.kaleyra.collaboration_suite_core_ui.notification.NotificationManager
import com.kaleyra.collaboration_suite_core_ui.utils.AppLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.merge
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

    private var wasPhoneBoxConnected = false
    private var wasChatBoxConnected = false

    private var scope = CoroutineScope(SupervisorJob(parentScope.coroutineContext[Job]) + Dispatchers.IO)

    init {
        syncWithAppLifecycle(parentScope)
    }

    /**
     * Connect the collaboration
     */
    fun connect(session: Collaboration.Session) {
        collaboration.connect(session)
        resume()
    }

    /**
     * Disconnect the collaboration
     */
    fun disconnect(clearSavedData: Boolean = false) {
        collaboration.disconnect(clearSavedData)
        pause()
    }

    private fun pause() {
        wasPhoneBoxConnected = collaboration.phoneBox.state.value.let { it !is PhoneBox.State.Disconnected && it !is PhoneBox.State.Disconnecting }
        wasChatBoxConnected = collaboration.chatBox.state.value.let { it !is ChatBox.State.Disconnected && it !is ChatBox.State.Disconnecting }
        collaboration.phoneBox.disconnect()
        collaboration.chatBox.disconnect()
        NotificationManager.cancelAll()
        scope.cancel()
    }

    private fun resume() {
        scope.cancel()
        scope = CoroutineScope(SupervisorJob(parentScope.coroutineContext[Job]) + Dispatchers.IO)
        syncWithCallState(scope)
        syncWithChatMessages(scope)
        if (wasPhoneBoxConnected) collaboration.phoneBox.connect()
        if (wasChatBoxConnected) collaboration.chatBox.connect()
    }

    private fun syncWithAppLifecycle(scope: CoroutineScope) {
        AppLifecycle.isInForeground
            .dropWhile { !it }
            .onEach { isInForeground ->
                if (isInForeground) performAction(Action.RESUME)
                else if (collaboration.phoneBox.call.replayCache.isEmpty()) performAction(Action.PAUSE)
            }
            .launchIn(scope)
    }

    private fun syncWithCallState(scope: CoroutineScope) {
        val callState = collaboration.phoneBox.call.flatMapLatest { it.state }
        scope.launch {
            combine(callState, AppLifecycle.isInForeground) { state, isInForeground ->
                state is Call.State.Disconnected.Ended && !isInForeground
            }.collectLatest {
                if (!it) return@collectLatest
                delay(300)
                performAction(Action.PAUSE)
            }
        }
    }

    private fun syncWithChatMessages(scope: CoroutineScope) {
        collaboration.chatBox.chats
            .flatMapLatest { chats -> chats.map { it.messages }.merge() }
            .filter { it.list.isNotEmpty() }
            .mapLatest { delay(3000); it }
            .onEach {
                val call = collaboration.phoneBox.call.replayCache.firstOrNull()
                if (AppLifecycle.isInForeground.value || (call != null && call.state.value !is Call.State.Disconnected.Ended)) return@onEach
                performAction(Action.PAUSE)
            }.launchIn(scope)
    }

    private fun performAction(action: Action) {
        synchronized(this) {
            if (action == lastAction) return
            when (action) {
                Action.RESUME -> resume()
                Action.PAUSE  -> pause()
            }
            lastAction = action
        }
    }
}