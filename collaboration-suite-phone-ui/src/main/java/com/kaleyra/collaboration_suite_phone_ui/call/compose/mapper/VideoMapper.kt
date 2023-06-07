package com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper

import com.kaleyra.collaboration_suite.phonebox.Input
import com.kaleyra.collaboration_suite_phone_ui.call.compose.ImmutableView
import com.kaleyra.collaboration_suite_phone_ui.call.compose.PointerUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.VideoUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.VideoMapper.mapToPointersUi
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

internal object VideoMapper {
    fun StateFlow<Input.Video?>.mapToVideoUi(): Flow<VideoUi?> {
        return flow {
            val initialValue = value?.let { video ->
                VideoUi(
                    video.id,
                    video.view.value?.let { ImmutableView(it) },
                    video.enabled.value,
                    video.isScreenShare(),
                    ImmutableList(emptyList())
                )
            }
            emit(initialValue)

            val flow = this@mapToVideoUi.filterIsInstance<Input.Video>()
            combine(
                flow.map { it.id },
                flow.flatMapLatest { it.view }.map { it?.let { ImmutableView(it) } },
                flow.flatMapLatest { it.enabled },
                flow.map { it.isScreenShare() },
                flow.mapToPointersUi()
            ) { id, view, enabled, isScreenShare, pointers ->
                val pointerList = ImmutableList(if (view != null && enabled) pointers else emptyList())
                VideoUi(id, view, enabled, isScreenShare, pointerList)
            }.collect {
                emit(it)
            }
        }
    }

    fun Flow<Input.Video>.mapToPointersUi(): Flow<List<PointerUi>> {
        val list = mutableMapOf<String, PointerUi>()
        return flow {
            emit(emptyList())
            combine(
                this@mapToPointersUi.flatMapLatest { it.events }.filterIsInstance<Input.Video.Event.Pointer>(),
                this@mapToPointersUi.map { it is Input.Video.Camera.Internal || it is Input.Video.Camera.Usb }
            ) { event, mirror ->
                if (event.action is Input.Video.Event.Pointer.Action.Idle) list.remove(event.producer.userId)
                else list[event.producer.userId] = event.mapToPointerUi(mirror)
                emit(list.values.toList())
            }.collect()
        }
    }

    fun Input.Video.Event.Pointer.mapToPointerUi(mirror: Boolean = false): PointerUi {
        return PointerUi(
            username = producer.displayName.value ?: "",
            x = if (mirror) 100 - position.x else position.x,
            y = position.y
        )
    }

    private fun Input.Video.isScreenShare() = this is Input.Video.Application || this is Input.Video.Screen

}