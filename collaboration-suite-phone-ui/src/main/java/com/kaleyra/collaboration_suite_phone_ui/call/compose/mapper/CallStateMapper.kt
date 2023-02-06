package com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper

import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite_phone_ui.call.compose.CallState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

internal object CallStateMapper {

    fun Flow<Call>.isConnected(): Flow<Boolean> =
        flatMapLatest { it.state }.map { it is Call.State.Connected }

    fun Flow<Call>.toCallStateUi(): Flow<CallState> {
        return combine(
            flatMapLatest { it.state },
            flatMapLatest { it.participants }
        ) { state, participants ->
            when {
                state is Call.State.Connected -> CallState.Connected
                state is Call.State.Reconnecting -> CallState.Reconnecting
                state is Call.State.Connecting && participants.me == participants.creator() -> CallState.Dialing
                state is Call.State.Connecting -> CallState.Connecting
                state is Call.State.Disconnected.Ended.AnsweredOnAnotherDevice -> CallState.Disconnected.Ended.AnsweredOnAnotherDevice
                state is Call.State.Disconnected.Ended.Declined -> CallState.Disconnected.Ended.Declined
                state is Call.State.Disconnected.Ended.LineBusy -> CallState.Disconnected.Ended.LineBusy
                state is Call.State.Disconnected.Ended.Timeout -> CallState.Disconnected.Ended.Timeout
                state is Call.State.Disconnected.Ended.Error.Server -> CallState.Disconnected.Ended.Error.Server
                state is Call.State.Disconnected.Ended.Error.Unknown -> CallState.Disconnected.Ended.Error.Unknown
                state is Call.State.Disconnected.Ended.HungUp -> CallState.Disconnected.Ended.HungUp
                state is Call.State.Disconnected.Ended.Kicked -> CallState.Disconnected.Ended.Kicked(state.userId)
                state == Call.State.Disconnected.Ended.Error -> CallState.Disconnected.Ended.Error
                state == Call.State.Disconnected.Ended -> CallState.Disconnected.Ended
                state is Call.State.Disconnected && participants.me != participants.creator() -> CallState.Ringing
                else -> CallState.Disconnected
            }
        }
    }
}