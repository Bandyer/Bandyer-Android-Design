package com.kaleyra.collaboration_suite_phone_ui.call.compose

import android.net.Uri
import com.kaleyra.collaboration_suite.phonebox.Input
import com.kaleyra.collaboration_suite.phonebox.Stream
import kotlinx.coroutines.flow.*

object CallModelMapper {

    fun StateFlow<Stream>.mapToStreamUi(
        displayName: StateFlow<String?>,
        displayImage: StateFlow<Uri?>
    ): Flow<StreamUi> {
        return combine(
            this@mapToStreamUi.flatMapLatest { it.video.mapToVideoUi() },
            displayName,
            displayImage
        ) { video, name, image ->
            StreamUi(
                video = video,
                username = name ?: "",
                avatar = image?.let { ImmutableUri(it) }
            )
        }
    }

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