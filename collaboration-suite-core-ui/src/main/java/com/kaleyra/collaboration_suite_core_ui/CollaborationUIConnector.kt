package com.kaleyra.collaboration_suite_core_ui

import com.kaleyra.collaboration_suite.chatbox.ChatBox
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite.phonebox.PhoneBox
import com.kaleyra.collaboration_suite_core_ui.utils.AppLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class CollaborationUIConnector(val collaboration: CollaborationUI, parentScope: CoroutineScope) {

    private var wasPhoneBoxConnected = false
    private var wasChatBoxConnected = false

    private var scope = CoroutineScope(SupervisorJob(parentScope.coroutineContext[Job]) + Dispatchers.Main)

    init {
        syncWithAppLifecycle(scope)
        syncWithCallState(scope)
    }

    fun connect() {
        collaboration.phoneBox.connect()
        collaboration.chatBox.connect()
    }

    fun disconnect() {
        wasPhoneBoxConnected = collaboration.phoneBox.state.value.let { it !is PhoneBox.State.Disconnected && it !is PhoneBox.State.Disconnecting }
        wasChatBoxConnected = collaboration.chatBox.state.value.let { it !is ChatBox.State.Disconnected && it !is ChatBox.State.Disconnecting }
        collaboration.phoneBox.disconnect()
        collaboration.chatBox.disconnect()
    }

    fun dispose(clearSavedData: Boolean = true) {
        collaboration.phoneBox.disconnect()
        collaboration.chatBox.disconnect(clearSavedData)
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
                else if (collaboration.phoneBox.call.replayCache.isEmpty()) disconnect() }
            .launchIn(scope)
    }

    private fun syncWithCallState(scope: CoroutineScope) {
        collaboration.phoneBox.call
            .flatMapLatest { it.state }
            .onEach {
                if (it !is Call.State.Disconnected.Ended || AppLifecycle.isInForeground.value) return@onEach
                disconnect()
            }.launchIn(scope)
    }
}