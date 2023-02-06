package com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper

import com.kaleyra.collaboration_suite.phonebox.Input
import com.kaleyra.collaboration_suite_phone_ui.call.compose.VideoUi
import kotlinx.coroutines.flow.*

internal object VideoMapper {
    fun StateFlow<Input.Video?>.mapToVideoUi(): Flow<VideoUi?> {
        return flow {
            val initialValue = value?.let {
                VideoUi(it.id, it.view.value, it.enabled.value)
            }
            emit(initialValue)

            val flow = this@mapToVideoUi.filter { it != null }
            combine(
                flow.map { it!!.id },
                flow.flatMapLatest { it!!.view },
                flow.flatMapLatest { it!!.enabled }
            ) { id, view, enabled ->
                VideoUi(id, view, enabled)
            }.collect {
                emit(it)
            }
        }
    }
}