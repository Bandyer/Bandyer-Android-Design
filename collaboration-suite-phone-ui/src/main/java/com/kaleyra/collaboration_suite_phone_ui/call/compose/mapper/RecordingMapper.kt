package com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper

import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite_core_ui.CallUI
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.RecordingMapper.mapToRecordingStateUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.recording.model.RecordingStateUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.recording.model.RecordingTypeUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.recording.model.RecordingUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.usermessages.model.RecordingMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

internal object RecordingMapper {

    fun Flow<Call>.toRecordingTypeUi(): Flow<RecordingTypeUi> =
        this.map { it.extras.recording }
            .map { it.type.mapToRecordingTypeUi() }
            .distinctUntilChanged()

    fun Flow<Call>.toRecordingStateUi(): Flow<RecordingStateUi> =
        this.map { it.extras.recording }
            .flatMapLatest { it.state }
            .map { it.mapToRecordingStateUi() }
            .distinctUntilChanged()

    fun Flow<Call>.toRecordingMessage(): Flow<RecordingMessage> =
        this.
            map { it.extras.recording }
            .flatMapLatest { it.state }
            .map { it.mapToRecordingMessage() }
            .distinctUntilChanged()

    fun Flow<Call>.toRecordingUi(): Flow<RecordingUi> =
        combine(toRecordingTypeUi(), toRecordingStateUi()) { type, state ->
            RecordingUi(type, state)
        }.distinctUntilChanged()

    fun Call.Recording.Type.mapToRecordingTypeUi(): RecordingTypeUi =
        when (this) {
            is Call.Recording.Type.OnConnect -> RecordingTypeUi.OnConnect
            is Call.Recording.Type.OnDemand -> RecordingTypeUi.OnDemand
            else -> RecordingTypeUi.Never
        }

    fun Call.Recording.State.mapToRecordingStateUi(): RecordingStateUi =
        when(this) {
            is Call.Recording.State.Started -> RecordingStateUi.Started
            Call.Recording.State.Stopped -> RecordingStateUi.Stopped
            is Call.Recording.State.Stopped.Error -> RecordingStateUi.Error
        }

    fun Call.Recording.State.mapToRecordingMessage(): RecordingMessage =
        when (this) {
            is Call.Recording.State.Started -> RecordingMessage.Started()
            Call.Recording.State.Stopped -> RecordingMessage.Stopped()
            is Call.Recording.State.Stopped.Error -> RecordingMessage.Failed()
        }
}