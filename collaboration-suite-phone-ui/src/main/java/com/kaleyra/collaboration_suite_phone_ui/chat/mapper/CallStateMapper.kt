package com.kaleyra.collaboration_suite_phone_ui.chat.mapper

import com.kaleyra.collaboration_suite.conference.Call
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

object CallStateMapper {

    fun Flow<Call>.hasActiveCall(): Flow<Boolean> =
        flatMapLatest { it.state }.map { it !is Call.State.Disconnected.Ended }
}