package com.bandyer.video_android_glass_ui

import kotlinx.coroutines.flow.StateFlow

interface Participant: User {
    val state: StateFlow<State>

    sealed class State {

        object InCall: State()

        sealed class Online: State() {

            companion object : Online() {
                override fun hashCode(): Int = "Online".hashCode()
                override fun equals(other: Any?) = other !is Invited && other is Online
                override fun toString() = "Online"
            }

            object Invited: Online()
        }

        sealed class Offline: State() {

            companion object : Offline() {
                override fun hashCode(): Int = "Offline".hashCode()
                override fun equals(other: Any?) = other !is Invited && other is Offline
                override fun toString() = "Offline"
            }

            object Invited: Offline()
        }
    }
}

sealed class CallParticipant(override val userAlias: String, override val avatarUrl: String, override val state: StateFlow<Participant.State>, open val streams: StateFlow<Stream>): Participant {

    data class Me(override val userAlias: String, override val avatarUrl: String, override val state: StateFlow<Participant.State>, override val streams: StateFlow<Stream>) : CallParticipant(userAlias, avatarUrl, state, streams)

    data class Other(override val userAlias: String, override val avatarUrl: String, override val state: StateFlow<Participant.State>, override val streams: StateFlow<Stream>) : CallParticipant(userAlias, avatarUrl, state, streams)
}




