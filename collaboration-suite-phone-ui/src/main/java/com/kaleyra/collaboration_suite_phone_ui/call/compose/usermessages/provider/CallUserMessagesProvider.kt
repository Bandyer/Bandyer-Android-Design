package com.kaleyra.collaboration_suite_phone_ui.call.compose.usermessages.provider

import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite_core_ui.CallUI
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.InputMapper.hasBeenMutedBy
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.RecordingMapper.toRecordingStateUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.recording.model.RecordingStateUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.usermessages.model.MutedMessage
import com.kaleyra.collaboration_suite_phone_ui.call.compose.usermessages.model.RecordingMessage
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.plus

object CallUserMessagesProvider {

    private var coroutineScope: CoroutineScope? = null

    private val recordingMessageChannel = Channel<RecordingMessage>()
    val recordingUserMessage: Flow<RecordingMessage> = recordingMessageChannel.receiveAsFlow()

    private val mutedMessageChannel = Channel<MutedMessage>()
    val mutedUserMessage: Flow<MutedMessage> = mutedMessageChannel.receiveAsFlow()

    fun start(call: Flow<CallUI>, scope: CoroutineScope = MainScope() + CoroutineName("CallUserMessagesProvider")) {
        if (coroutineScope != null) dispose()
        coroutineScope = scope
        observeRecordingEvents(call, coroutineScope!!)
        observeMutedEvents(call, coroutineScope!!)
    }

    fun dispose() {
        coroutineScope?.cancel()
        coroutineScope = null
    }

    private fun observeRecordingEvents(call: Flow<Call>, scope: CoroutineScope) {
        call
            .toRecordingStateUi()
            .dropWhile { it == RecordingStateUi.Stopped }
            .onEach { state ->
                val value = when (state) {
                    RecordingStateUi.Started -> RecordingMessage.Started()
                    RecordingStateUi.Stopped -> RecordingMessage.Stopped()
                    RecordingStateUi.Error -> RecordingMessage.Failed()
                }
                recordingMessageChannel.send(value)
            }.launchIn(scope)
    }

    private fun observeMutedEvents(call: Flow<Call>, scope: CoroutineScope) {
        call
            .hasBeenMutedBy()
            .onEach { mutedMessageChannel.send(MutedMessage(it)) }
            .launchIn(scope)
    }
}