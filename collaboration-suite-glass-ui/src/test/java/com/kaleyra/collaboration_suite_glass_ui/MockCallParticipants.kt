package com.kaleyra.collaboration_suite_glass_ui

import androidx.fragment.app.FragmentActivity
import com.kaleyra.collaboration_suite.phonebox.CallParticipant
import com.kaleyra.collaboration_suite.phonebox.CallParticipants
import com.kaleyra.collaboration_suite.phonebox.Input
import com.kaleyra.collaboration_suite.phonebox.Stream
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.*

class MockCallParticipants : CallParticipants {
    override val me: CallParticipant.Me = MockCallParticipantMe()
    override val others: List<CallParticipant> =
        listOf(MockCallParticipant(), MockCallParticipant(), MockCallParticipant())
    override fun creator(): CallParticipant = me
}

class MockCallParticipantMe : CallParticipant.Me {
    override val state: StateFlow<CallParticipant.State> = participantState
    override val streams: StateFlow<List<Stream.Mutable>> = participantStreams
    override val userId: String = UUID.randomUUID().toString()
    override fun addStream(context: FragmentActivity, id: String): Stream.Mutable = mutableStream
    override fun removeStream(stream: Stream.Mutable): Boolean = false
}

class MockCallParticipant : CallParticipant {
    override val state: StateFlow<CallParticipant.State> = participantState
    override val streams: StateFlow<List<Stream>> = participantStreams
    override val userId: String = UUID.randomUUID().toString()
}

val participantState = MutableStateFlow(CallParticipant.State.NOT_IN_CALL)
val participantStreams = MutableStateFlow<List<Stream.Mutable>>(listOf())
val mutableStream = object : Stream.Mutable {
    override val audio: MutableStateFlow<Input.Audio?> = MutableStateFlow(null)
    override val id: String = UUID.randomUUID().toString()
    override val state: StateFlow<Stream.State> = MutableStateFlow(Stream.State.Open)
    override val video: MutableStateFlow<Input.Video.My?> = MutableStateFlow(null)
    override fun close() = Unit
    override fun open() = Unit
}