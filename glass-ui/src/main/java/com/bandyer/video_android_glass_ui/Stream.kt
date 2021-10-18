package com.bandyer.video_android_glass_ui

import kotlinx.coroutines.flow.Flow

data class Stream(
    val id: String,
    val video: Flow<Video?>,
    val audio: Flow<Audio?>,
    val state: Flow<State>
) {

    sealed class State {

        object Opening : State()

        object Open : State()

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




