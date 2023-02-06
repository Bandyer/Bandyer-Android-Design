package com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper

import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite_phone_ui.call.compose.Recording
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

internal object RecordingMapper {

    fun Flow<Call>.toRecordingUi(): Flow<Recording?> =
        map { it.extras.recording }
            .map {
                when (it.type) {
                    is Call.Recording.Type.OnConnect -> Recording.OnConnect
                    is Call.Recording.Type.OnDemand -> Recording.OnDemand
                    else -> null
                }
            }

    fun Flow<Call>.isRecording(): Flow<Boolean> =
        flatMapLatest { it.extras.recording.state }.map { it == Call.Recording.State.Started }
}