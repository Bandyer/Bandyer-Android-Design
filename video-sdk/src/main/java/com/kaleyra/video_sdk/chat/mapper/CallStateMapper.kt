package com.kaleyra.video_sdk.chat.mapper

import com.kaleyra.video.conference.Call
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

object CallStateMapper {

    fun Flow<Call>.hasActiveCall(): Flow<Boolean> =
        flatMapLatest { it.state }.map { it !is Call.State.Disconnected.Ended }
}