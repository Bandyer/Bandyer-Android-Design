package com.kaleyra.collaboration_suite_phone_ui.call.compose

import android.net.Uri
import com.kaleyra.collaboration_suite.phonebox.CallParticipants
import com.kaleyra.collaboration_suite.phonebox.Input
import com.kaleyra.collaboration_suite.phonebox.Inputs
import kotlinx.coroutines.flow.*

object CallModelMapper {

    // TODO move this utility functions in another object (Reducer??)
    fun Flow<Inputs>.toMyCamera(): Flow<Input.Video.Camera> =
        flatMapLatest { it.availableInputs }.map {
            it.filterIsInstance<Input.Video.Camera>().last()
        }

    fun Flow<CallParticipants>.toMyDisplayName(): Flow<String?> =
        flatMapLatest { it.me.displayName }

    fun Flow<CallParticipants>.toMyDisplayImage(): Flow<Uri?> = flatMapLatest { it.me.displayImage }

//    fun Flow<CallUI>.mapToMyStreamUi(scope: CoroutineScope): Flow<StreamUi> {
//        val inputs = map { it.inputs }
//        val participants = flatMapLatest { it.participants }.onEach {
//            it.list.onEach {
//                it.streams.value.first().video.value
//            }
//        }
//        val myCamera = inputs.toMyCamera()
//        myCamera.mapToVideoUi().onEach {
//
//        }.launchIn(scope)
//        val myDisplayName = participants.toMyDisplayName()
//        val myDisplayImage = participants.toMyDisplayImage()
//
//        return combine(videoUi, myDisplayName, myDisplayImage) { video, name, image ->
//            StreamUi(
//                video = video,
//                username = name ?: "",
//                avatar = image?.let { ImmutableUri(it) },
//            )
//        }
//    }

//    fun Flow<CallParticipant>.mapToStreamsUi(): Flow<List<StreamUi>> {
//        val displayName = flatMapLatest { it.displayName }
//        val displayImage = flatMapLatest { it.displayImage }
//        val streams = flatMapLatest { it.streams }
//        combine(streams, displayName, displayImage) { streams, name, image ->
//            streams.forEach {
//                StreamUi(
//                    video = it.video, username =, avatar =
//                )
//            }
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