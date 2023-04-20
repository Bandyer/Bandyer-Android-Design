package com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper

import com.kaleyra.collaboration_suite.phonebox.Input
import com.kaleyra.collaboration_suite_phone_ui.call.compose.ImmutableView
import com.kaleyra.collaboration_suite_phone_ui.call.compose.PointerUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.VideoUi
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

internal object VideoMapper {
    fun StateFlow<Input.Video?>.mapToVideoUi(): Flow<VideoUi?> {
        return flow {
            val initialValue = value?.let { video ->
                VideoUi(video.id, video.view.value?.let { ImmutableView(it) }, video.enabled.value, ImmutableList(emptyList()))
            }
            emit(initialValue)

            val flow = this@mapToVideoUi.filterIsInstance<Input.Video>()
            combine(
                flow.map { it.id },
                flow.flatMapLatest { it.view }.map { it?.let { ImmutableView(it) }},
                flow.flatMapLatest { it.enabled },
                flow.mapToPointersUi()
            ) { id, view, enabled, pointers ->
                val pointerList = ImmutableList(if (view != null && enabled) pointers else emptyList())
                VideoUi(id, view, enabled, pointerList)
            }.collect {
                emit(it)
            }
        }
    }

    fun Flow<Input.Video>.mapToPointersUi(): Flow<List<PointerUi>> {
        val list = mutableMapOf<String, PointerUi>()
        return flow {
            emit(emptyList())
            this@mapToPointersUi.flatMapLatest { it.events }
                .filterIsInstance<Input.Video.Event.Pointer>()
                .collect {
                    if (it.action is Input.Video.Event.Pointer.Action.Idle) list.remove(it.producer.userId)
                    else list[it.producer.userId] = it.mapToPointerUi()
                    emit(list.values.toList())
                }
        }
    }

    fun Input.Video.Event.Pointer.mapToPointerUi(): PointerUi {
        return PointerUi(
            username = producer.displayName.value ?: "",
            x = position.x,
            y = position.y
        )
    }
}