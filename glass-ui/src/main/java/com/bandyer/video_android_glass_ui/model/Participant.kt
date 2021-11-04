package com.bandyer.video_android_glass_ui.model

import kotlinx.coroutines.flow.Flow

interface Participant : User {
    val state: Flow<State>

    interface State
}

interface CallParticipant : Participant {

    val streams: Flow<List<Stream>>

    override val state: Flow<State>

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

data class PhoneCallParticipant(
    override val id: String,
    override val username: String,
    override val avatarUrl: String?,
    override val state: Flow<CallParticipant.State>,
    override val streams: Flow<List<Stream>>
) : CallParticipant


