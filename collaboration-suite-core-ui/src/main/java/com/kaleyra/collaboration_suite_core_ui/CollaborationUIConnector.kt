package com.kaleyra.collaboration_suite_core_ui

import com.kaleyra.collaboration_suite.chatbox.ChatBox
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite.phonebox.PhoneBox
import com.kaleyra.collaboration_suite_core_ui.utils.AppLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class CollaborationUIConnector(val collaboration: CollaborationUI, val scope: CoroutineScope) {

    private var wasPhoneBoxConnected = false
    private var wasChatBoxConnected = false

    init {
        var disconnectJob: Job? = null
        AppLifecycle.isInForeground.onEach { isInForeground ->
            if (isInForeground) resume()
            else if (collaboration.phoneBox.call.replayCache.isEmpty()) disconnect()
            disconnectJob?.cancel()
            disconnectJob = disconnectOnCallEndedInBackground(isInForeground)
        }.launchIn(scope)
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
    }

    private fun resume() {
        if (wasPhoneBoxConnected) collaboration.phoneBox.connect()
        if (wasChatBoxConnected) collaboration.chatBox.connect()
    }

    private fun disconnectOnCallEndedInBackground(isInForeground: Boolean) = collaboration.phoneBox.call.onEach calls@{ call ->
        if (call.state.value !is Call.State.Disconnected.Ended || isInForeground) return@calls
        disconnect()
    }.launchIn(scope)
}