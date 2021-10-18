package com.bandyer.video_android_glass_ui

import kotlinx.coroutines.flow.StateFlow

data class Call(
    val id: String,
    val participants: List<CallParticipant>,
    val state: StateFlow<State>,
    val duration: StateFlow<Long?>,
    val recording: Boolean
) {
    sealed class State {
        object Dialing : State()
        object Ringing : State()
        object Connecting : State()
        object Reconnecting : State()
        object Connected : State()
        sealed class Disconnected : State() {

            companion object : Disconnected() {
                override fun hashCode(): Int = "Disconnected".hashCode()
                override fun equals(other: Any?) = other is Disconnected && other !is Ended && other !is Error
                override fun toString() = "Disconnected"
            }

            data class Ended(val reason: String) : Disconnected()
            data class Error(val reason: String) : Disconnected()
        }
    }
}