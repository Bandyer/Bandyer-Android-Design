package com.bandyer.video_android_glass_ui.model

import kotlinx.coroutines.flow.Flow

interface Stream {

    val id: String

    val video: Flow<Input.Video?>

    val audio: Flow<Input.Audio?>

    val state: Flow<State>

    sealed class State {

        object Open : State()

        object Live : State()

        sealed class Closed : State() {

            companion object : Closed() {
                override fun hashCode(): Int = "Closed".hashCode()
                override fun equals(other: Any?) = other is Closed && other !is Error
                override fun toString() = "Closed"
            }

            data class Error(val reason: String) : Closed()
        }
    }
}

data class ParticipantStream(
    override val id: String,
    override val video: Flow<Input.Video?>,
    override val audio: Flow<Input.Audio?>,
    override val state: Flow<Stream.State>
) : Stream



