package com.bandyer.video_android_glass_ui.model

import kotlinx.coroutines.flow.Flow

data class Call(
    val id: String,
    val participants: Flow<CallParticipants>,
    val state: Flow<State>,
    val duration: Flow<Long>,
    val isRecording: Flow<Boolean>
) {
    sealed class State {
        object Connected : State()
        object Connecting : State()
        object Reconnecting : State()
        sealed class Disconnected : State() {

            companion object : Disconnected() {
                override fun hashCode(): Int = "Disconnected".hashCode()
                override fun equals(other: Any?) = other is Disconnected && other !is Ended
                override fun toString() = "Disconnected"
            }

            sealed class Ended(open val reason: String) : Disconnected() {
                companion object : Ended("The call has ended") {
                    override fun hashCode(): Int = "Ended".hashCode()
                    override fun equals(other: Any?) = other !is Error && other is Ended
                    override fun toString() = "Ended: $reason"
                }

                data class HangUp(override val reason: String) : Ended(reason)
                data class Declined(override val reason: String) : Ended(reason)
                data class AnsweredOnAnotherDevice(override val reason: String) : Ended(reason)

                sealed class Error(reason: String) : Ended(reason) {
                    companion object : Error("An error occurred") {
                        override fun hashCode(): Int = "Error".hashCode()
                        override fun equals(other: Any?) = other is Error
                        override fun toString() = "Error: $reason"
                    }

                    data class Connection(override val reason: String) : Error(reason)
                    data class Timeout(override val reason: String) : Error(reason)
                    data class Server(override val reason: String) : Error(reason)
                    data class Client(override val reason: String) : Error(reason)
                    data class Unknown(override val reason: String) : Error(reason)
                }

            }
        }
    }
}