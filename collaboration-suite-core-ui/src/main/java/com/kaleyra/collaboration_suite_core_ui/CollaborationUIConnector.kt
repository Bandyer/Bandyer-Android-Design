package com.kaleyra.collaboration_suite_core_ui

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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.sample

/**
 * The collaboration UI connector
 *
 * @property collaboration The collaboration UI
 * @property scope The coroutine scope
 * @constructor
 */
internal class CollaborationUIConnector(val collaboration: CollaborationUI, parentScope: CoroutineScope) {

    private var wasPhoneBoxConnected = false
    private var wasChatBoxConnected = false

    private var scope = CoroutineScope(SupervisorJob(parentScope.coroutineContext[Job]) + Dispatchers.IO)

    init {
        syncWithAppLifecycle(scope)
        syncWithCallState(scope)
        syncWithChatMessages(scope)
    }

    /**
     * Connect the collaboration
     */
    fun connect() {
        collaboration.phoneBox.connect()
        collaboration.chatBox.connect()
    }

    /**
     * Disconnect the collaboration
     */
    fun disconnect() {
        wasPhoneBoxConnected = collaboration.phoneBox.state.value.let { it !is PhoneBox.State.Disconnected && it !is PhoneBox.State.Disconnecting }
        wasChatBoxConnected = collaboration.chatBox.state.value.let { it !is ChatBox.State.Disconnected && it !is ChatBox.State.Disconnecting }
        collaboration.phoneBox.disconnect()
        collaboration.chatBox.disconnect()
    }

    /**
     * Dispose the collaboration
     */
    fun dispose(clearSavedData: Boolean = true) {
        collaboration.phoneBox.dispose()
        collaboration.chatBox.dispose(clearSavedData)
        NotificationManager.cancelAll()
        scope.cancel()
    }

    private fun resume() {
        if (wasPhoneBoxConnected) collaboration.phoneBox.connect()
        if (wasChatBoxConnected) collaboration.chatBox.connect()
    }

    private fun syncWithAppLifecycle(scope: CoroutineScope) {
        AppLifecycle.isInForeground
            .dropWhile { !it }
            .onEach { isInForeground ->
                if (isInForeground) resume()
                else if (collaboration.phoneBox.call.replayCache.isEmpty()) disconnect()
            }
            .launchIn(scope)
    }

    private fun syncWithCallState(scope: CoroutineScope) {
        val callState = collaboration.phoneBox.call.flatMapLatest { it.state }
        combine(callState, AppLifecycle.isInForeground.sample(300)) { state, isInForeground ->
            if (state !is Call.State.Disconnected.Ended || isInForeground) return@combine
            disconnect()
        }.launchIn(scope)
    }

    private fun syncWithChatMessages(scope: CoroutineScope) {
        collaboration.chatBox.chats
            .flatMapLatest { chats -> chats.map { it.messages }.merge() }
            .filter { it.list.isNotEmpty() }
            .mapLatest { delay(3000); it }
            .onEach {
                val call = collaboration.phoneBox.call.replayCache.firstOrNull()
                if (AppLifecycle.isInForeground.value || (call != null && call.state.value !is Call.State.Disconnected.Ended)) return@onEach
                disconnect()
            }.launchIn(scope)
    }
}