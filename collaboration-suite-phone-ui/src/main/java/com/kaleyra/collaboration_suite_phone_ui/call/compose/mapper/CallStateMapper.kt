package com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper

import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite_phone_ui.call.compose.CallStateUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.StreamMapper.amIAlone
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

internal object CallStateMapper {

    fun Flow<Call>.isConnected(): Flow<Boolean> =
        flatMapLatest { it.state }.map { it is Call.State.Connected }

    fun Flow<Call>.toCallStateUi(): Flow<CallStateUi> {
        var current: CallStateUi = CallStateUi.Disconnected
        return combine(
            flatMapLatest { it.state },
            flatMapLatest { it.participants },
            amIAlone()
        ) { state, participants, amIAlone ->
            current = when {
                current is CallStateUi.Dialing && amIAlone -> CallStateUi.Dialing
                current is CallStateUi.Ringing && amIAlone -> CallStateUi.Ringing
                state is Call.State.Connected -> CallStateUi.Connected
                state is Call.State.Reconnecting -> CallStateUi.Reconnecting
                state is Call.State.Connecting && participants.me == participants.creator() -> CallStateUi.Dialing
                state is Call.State.Connecting -> CallStateUi.Connecting
                state is Call.State.Disconnected.Ended.AnsweredOnAnotherDevice -> CallStateUi.Disconnected.Ended.AnsweredOnAnotherDevice
                state is Call.State.Disconnected.Ended.Declined -> CallStateUi.Disconnected.Ended.Declined
                state is Call.State.Disconnected.Ended.LineBusy -> CallStateUi.Disconnected.Ended.LineBusy
                state is Call.State.Disconnected.Ended.Timeout -> CallStateUi.Disconnected.Ended.Timeout
                state is Call.State.Disconnected.Ended.Error.Server -> CallStateUi.Disconnected.Ended.Error.Server
                state is Call.State.Disconnected.Ended.Error.Unknown -> CallStateUi.Disconnected.Ended.Error.Unknown
                state is Call.State.Disconnected.Ended.HungUp -> CallStateUi.Disconnected.Ended.HungUp
                state is Call.State.Disconnected.Ended.Kicked -> CallStateUi.Disconnected.Ended.Kicked(state.userId)
                state == Call.State.Disconnected.Ended.Error -> CallStateUi.Disconnected.Ended.Error
                state == Call.State.Disconnected.Ended -> CallStateUi.Disconnected.Ended
                state is Call.State.Disconnected && participants.me != participants.creator() -> CallStateUi.Ringing
                state == Call.State.Disconnected -> CallStateUi.Disconnected
                else -> current
            }
            current
        }
    }
}