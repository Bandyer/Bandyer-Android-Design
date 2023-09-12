package com.kaleyra.collaboration_suite_phone_ui.call.mapper

import com.kaleyra.collaboration_suite.conference.Call
import com.kaleyra.collaboration_suite_phone_ui.call.model.RecordingStateUi
import com.kaleyra.collaboration_suite_phone_ui.call.model.RecordingTypeUi
import com.kaleyra.collaboration_suite_phone_ui.call.model.RecordingUi
import com.kaleyra.collaboration_suite_phone_ui.common.usermessages.model.RecordingMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

internal object RecordingMapper {

    fun Flow<Call>.toRecordingTypeUi(): Flow<RecordingTypeUi> =
        this.flatMapLatest { it.recording }
            .map { it.type.mapToRecordingTypeUi() }
            .distinctUntilChanged()

    fun Flow<Call>.toRecordingStateUi(): Flow<RecordingStateUi> =
        this.flatMapLatest { it.recording }
            .flatMapLatest { it.state }
            .map { it.mapToRecordingStateUi() }
            .distinctUntilChanged()

    fun Flow<Call>.toRecordingMessage(): Flow<RecordingMessage> =
        this.flatMapLatest { it.recording }
            .flatMapLatest { it.state }
            .map { it.mapToRecordingMessage() }
            .distinctUntilChanged()

    fun Flow<Call>.toRecordingUi(): Flow<RecordingUi> =
        combine(toRecordingTypeUi(), toRecordingStateUi()) { type, state ->
            RecordingUi(type, state)
        }.distinctUntilChanged()

    fun Call.Recording.Type.mapToRecordingTypeUi(): RecordingTypeUi =
        when (this) {
            Call.Recording.Type.OnConnect -> RecordingTypeUi.OnConnect
            Call.Recording.Type.OnDemand -> RecordingTypeUi.OnDemand
            else -> RecordingTypeUi.Never
        }

    fun Call.Recording.State.mapToRecordingStateUi(): RecordingStateUi =
        when(this) {
            is Call.Recording.State.Started -> RecordingStateUi.Started
            Call.Recording.State.Stopped -> RecordingStateUi.Stopped
            is Call.Recording.State.Stopped.Error -> RecordingStateUi.Error
        }

    private fun Call.Recording.State.mapToRecordingMessage(): RecordingMessage =
        when (this) {
            is Call.Recording.State.Started -> RecordingMessage.Started
            Call.Recording.State.Stopped -> RecordingMessage.Stopped
            is Call.Recording.State.Stopped.Error -> RecordingMessage.Failed
        }
}