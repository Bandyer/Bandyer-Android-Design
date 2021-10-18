package com.bandyer.video_android_glass_ui

import kotlinx.coroutines.flow.StateFlow

interface Participant : User {
    val state: StateFlow<State>

    interface State
}

sealed class CallParticipant(
    val isLocalUser: Boolean,
    override val userAlias: String,
    override val avatarUrl: String,
    override val state: StateFlow<State>,
    val streams: StateFlow<Stream>
) : Participant {

    sealed class State : Participant.State {

        sealed class Online : State() {
            companion object : Online() {
                override fun hashCode(): Int = "Online".hashCode()
                override fun equals(other: Any?) = other is Online && other !is InCall && other !is Invited
                override fun toString() = "Online"
            }

            object InCall : Online()
            object Invited : Online()
        }

        open class Offline(open val lastSeen: Long) : State() {
            data class Invited(override val lastSeen: Long) : Offline(lastSeen)
        }
    }
}



