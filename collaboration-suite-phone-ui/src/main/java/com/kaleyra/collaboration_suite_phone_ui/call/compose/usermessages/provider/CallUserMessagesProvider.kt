package com.kaleyra.collaboration_suite_phone_ui.call.compose.usermessages.provider

import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.InputMapper.hasBeenMutedBy
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.RecordingMapper.toRecordingStateUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.recording.model.RecordingStateUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.usermessages.model.MutedMessage
import com.kaleyra.collaboration_suite_phone_ui.call.compose.usermessages.model.RecordingMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CallUserMessagesProvider(private val call: Flow<Call>) {

    fun recordingUserMessage(): Flow<RecordingMessage> = call
        .toRecordingStateUi()
        .map { state ->
            when (state) {
                RecordingStateUi.Started -> RecordingMessage.Started()
                RecordingStateUi.Stopped -> RecordingMessage.Stopped()
                RecordingStateUi.Error -> RecordingMessage.Failed()
            }
        }

    fun mutedUserMessage(): Flow<MutedMessage> =
        call.hasBeenMutedBy().map { MutedMessage(it) }
}