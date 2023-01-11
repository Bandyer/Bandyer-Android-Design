package com.kaleyra.collaboration_suite_phone_ui

import android.net.Uri
import com.kaleyra.collaboration_suite.Contact
import com.kaleyra.collaboration_suite.chatbox.*
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite_core_ui.*
import com.kaleyra.collaboration_suite_core_ui.model.UsersDescription
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

val phoneBoxMock = mockk<PhoneBoxUI>(relaxed = true)
val chatBoxMock = mockk<ChatBoxUI>(relaxed = true)
val messagesUIMock = mockk<MessagesUI>(relaxed = true)
val usersDescriptionMock = mockk<UsersDescription>(relaxed = true)
val callMock = mockk<CallUI>(relaxed = true)
val chatMock = mockk<ChatUI>(relaxed = true)

val callState = MutableStateFlow<Call.State>(Call.State.Connected)
val chatBoxState = MutableStateFlow<ChatBox.State>(ChatBox.State.Connected)
val otherParticipantState = MutableStateFlow<ChatParticipant.State>(ChatParticipant.State.Invited)
val otherParticipantEvents =
    MutableStateFlow<ChatParticipant.Event>(ChatParticipant.Event.Typing.Idle)

val now: Instant = Instant.now()
val yesterday: Instant = now.minus(1, ChronoUnit.DAYS)

val otherParticipantMock = object : ChatParticipant {
    override val state: StateFlow<ChatParticipant.State> = otherParticipantState
    override val events: StateFlow<ChatParticipant.Event> = otherParticipantEvents
    override val userId: String = "userId"
    override val restrictions: Contact.Restrictions = mockk()
    override val displayName: StateFlow<String?> = MutableStateFlow(null)
    override val displayImage: StateFlow<Uri?> = MutableStateFlow(null)

}

val chatParticipantsMock = object : ChatParticipants {
    override val me: ChatParticipant.Me = mockk(relaxed = true)
    override val others: List<ChatParticipant> = listOf(otherParticipantMock)
    override val list: List<ChatParticipant> = others + me
    override fun creator(): ChatParticipant? = null
}

val myMessageMock = object : Message {
    override val id: String = "myId"
    override val creator: ChatParticipant = mockk()
    override val creationDate: Date = Date(now.toEpochMilli())
    override val content: Message.Content = Message.Content.Text("text")
    override val state: StateFlow<Message.State> = MutableStateFlow(Message.State.Read())
}

val otherReadMessageMock = object : OtherMessage {
    override val id: String = "otherId0"
    override val creator: ChatParticipant = mockk()
    override val creationDate: Date = Date(now.toEpochMilli())
    override val content: Message.Content = mockk()
    override val state: StateFlow<Message.State> = MutableStateFlow(Message.State.Read())
    override fun markAsRead() = Unit
}

val otherUnreadMessageMock1 = spyk(
    object : OtherMessage {
        override val id: String = "otherId1"
        override val creator: ChatParticipant = mockk()
        override val creationDate: Date = Date(yesterday.toEpochMilli())
        override val content: Message.Content = mockk()
        override val state: StateFlow<Message.State> = MutableStateFlow(Message.State.Received())
        override fun markAsRead() = Unit
    }
)

val otherUnreadMessageMock2 = spyk(
    object : OtherMessage {
        override val id: String = "otherId2"
        override val creator: ChatParticipant = mockk()
        override val creationDate: Date = Date(now.toEpochMilli())
        override val content: Message.Content = mockk()
        override val state: StateFlow<Message.State> = MutableStateFlow(Message.State.Received())
        override fun markAsRead() = Unit
    }
)


