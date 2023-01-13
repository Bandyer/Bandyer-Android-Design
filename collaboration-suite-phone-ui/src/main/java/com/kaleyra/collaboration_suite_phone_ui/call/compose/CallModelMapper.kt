package com.kaleyra.collaboration_suite_phone_ui.call.compose

import android.net.Uri
import com.kaleyra.collaboration_suite.phonebox.Input
import com.kaleyra.collaboration_suite.phonebox.Stream
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

object CallModelMapper {

    fun Flow<List<Stream>>.mapToStreamsUi(
        displayName: Flow<String?>,
        displayImage: Flow<Uri?>
    ): Flow<List<StreamUi>> {
        return flatMapLatest { streams ->
            val mutex = Mutex()
            val map = mutableMapOf<String, StreamUi>()

            streams
                .map { stream ->
                    val id = stream.id
                    val video = stream.video.mapToVideoUi()

                    combine(
                        video,
                        displayName,
                        displayImage
                    ) { video, name, image ->
                        StreamUi(
                            id = id,
                            video = video,
                            username = name ?: "",
                            avatar = image?.let { ImmutableUri(it) }
                        )
                    }
                }
                .merge()
                .transform { stream ->
                    mutex.withLock {
                        map[stream.id] = stream
                        val result = map.values.toList()
                        if (result.size == streams.size) emit(result)
                    }
                }
        }
    }

//    fun StateFlow<Stream>.mapToStreamUi(
//        displayName: StateFlow<String?>,
//        displayImage: StateFlow<Uri?>
//    ): Flow<StreamUi> {
//        return combine(
//            this@mapToStreamUi.flatMapLatest { it.video.mapToVideoUi() },
//            displayName,
//            displayImage
//        ) { video, name, image ->
//            StreamUi(
//                video = video,
//                username = name ?: "",
//                avatar = image?.let { ImmutableUri(it) }
//            )
//        }
//    }

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