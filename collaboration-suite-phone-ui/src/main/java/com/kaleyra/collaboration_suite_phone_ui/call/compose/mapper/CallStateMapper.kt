package com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper

import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite.phonebox.CallParticipants
import com.kaleyra.collaboration_suite_phone_ui.call.compose.CallStateUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.StreamMapper.amIAlone
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

internal object CallStateMapper {

    fun Flow<Call>.isConnected(): Flow<Boolean> =
        this.flatMapLatest { it.state }
            .map { it is Call.State.Connected }
            .distinctUntilChanged()

    fun Flow<Call>.toCallStateUi(): Flow<CallStateUi> =
        combine(
            flatMapLatest { it.state },
            flatMapLatest { it.participants },
            amIAlone()
        ) { state, participants, amIAlone ->
            when {
                isWaitingOthers(state, amIAlone) && participants.me == participants.creator() -> CallStateUi.Dialing
                isWaitingOthers(state, amIAlone) && participants.me != participants.creator() -> CallStateUi.Ringing(isConnecting = state != Call.State.Disconnected)
                isDialing(state, participants) -> CallStateUi.Dialing
                isRinging(state, participants) -> CallStateUi.Ringing(isConnecting = state != Call.State.Disconnected)
                state is Call.State.Connected -> CallStateUi.Connected
                state is Call.State.Reconnecting -> CallStateUi.Reconnecting
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
                else -> CallStateUi.Disconnected
            }
        }.distinctUntilChanged()

    private fun isDialing(state: Call.State, participants: CallParticipants) =
        state is Call.State.Connecting && participants.me == participants.creator()

    private fun isRinging(state: Call.State, participants: CallParticipants) =
        (state == Call.State.Disconnected || state is Call.State.Connecting) && participants.me != participants.creator()

    private fun isWaitingOthers(state: Call.State, amIAlone: Boolean) = state is Call.State.Connected && amIAlone

}