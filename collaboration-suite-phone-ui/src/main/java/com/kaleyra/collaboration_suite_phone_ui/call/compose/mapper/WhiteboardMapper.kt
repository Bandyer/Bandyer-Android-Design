package com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper

import com.kaleyra.collaboration_suite.phonebox.Whiteboard
import com.kaleyra.collaboration_suite_core_ui.CallUI
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

object WhiteboardMapper {

    fun Flow<CallUI>.isWhiteboardLoading(): Flow<Boolean> {
        return this.map { it.whiteboard }
            .flatMapLatest { it.state }
            .map { it is Whiteboard.State.Loading }
    }

    fun Flow<CallUI>.getWhiteboardTextEvents(): Flow<Whiteboard.Event.Text> {
        return this.map { it.whiteboard }
            .flatMapLatest { it.events }
            .filterIsInstance()
    }
}