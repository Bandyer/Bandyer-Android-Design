package com.kaleyra.collaboration_suite_phone_ui

import com.kaleyra.collaboration_suite.chatbox.*
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite_core_ui.CallUI
import com.kaleyra.collaboration_suite_core_ui.ChatBoxUI
import com.kaleyra.collaboration_suite_core_ui.MessagesUI
import com.kaleyra.collaboration_suite_core_ui.PhoneBoxUI
import com.kaleyra.collaboration_suite_core_ui.model.UsersDescription
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

val phoneBoxMock = mockk<PhoneBoxUI>()
val chatBoxMock = mockk<ChatBoxUI>()
val messagesUIMock = mockk<MessagesUI>()
val usersDescriptionMock = mockk<UsersDescription>()
val callMock = mockk<CallUI>()

val callState = MutableStateFlow<Call.State>(Call.State.Connected)
val chatBoxState = MutableStateFlow<ChatBox.State>(ChatBox.State.Connected)
val otherParticipantState = MutableStateFlow<ChatParticipant.State>(ChatParticipant.State.Invited)
val otherParticipantEvents = MutableStateFlow<ChatParticipant.Event>(ChatParticipant.Event.Typing.Idle)

val otherParticipantMock = object : ChatParticipant {
    override val state: StateFlow<ChatParticipant.State> = otherParticipantState
    override val events: StateFlow<ChatParticipant.Event> = otherParticipantEvents
    override val userId: String = "userId"

}

val chatParticipantsMock = object : ChatParticipants {
    override val me: ChatParticipant.Me = mockk()
    override val others: List<ChatParticipant> = listOf(otherParticipantMock)
    override val list: List<ChatParticipant> = others + me
    override fun creator(): ChatParticipant? = null
}

val now: Instant = Instant.now()
val myMessageMock = object : Message {
    override val id: String = "myId"
    override val creator: ChatParticipant = mockk()
    override val creationDate: Date = Date(now.toEpochMilli())
    override val content: Message.Content = Message.Content.Text("text")
    override val state: StateFlow<Message.State> = MutableStateFlow(Message.State.Read())
}

val yesterday: Instant = now.minus(1, ChronoUnit.DAYS)
val otherMessageMock = object : OtherMessage {
    override val id: String = "otherId"
    override val creator: ChatParticipant = mockk()
    override val creationDate: Date = Date(yesterday.toEpochMilli())
    override val content: Message.Content = Message.Content.Text("text")
    override val state: StateFlow<Message.State> = MutableStateFlow(Message.State.Received())
    override fun markAsRead() = Unit
}

val readMessageMock = object : OtherMessage {
    override val id: String = "id1"
    override val creator: ChatParticipant = mockk()
    override val creationDate: Date = Date()
    override val content: Message.Content = mockk()
    override val state: StateFlow<Message.State> = MutableStateFlow(Message.State.Read())
    override fun markAsRead() = Unit
}

val unreadMessageMock = object : OtherMessage {
    override val id: String = "id1"
    override val creator: ChatParticipant = mockk()
    override val creationDate: Date = Date()
    override val content: Message.Content = mockk()
    override val state: StateFlow<Message.State> = MutableStateFlow(Message.State.Received())
    override fun markAsRead() = Unit
}

val lastUnreadMessageMock = object : OtherMessage {
    override val id: String = "lastId"
    override val creator: ChatParticipant = mockk()
    override val creationDate: Date = Date()
    override val content: Message.Content = mockk()
    override val state: StateFlow<Message.State> = MutableStateFlow(Message.State.Received())
    override fun markAsRead() = Unit
}