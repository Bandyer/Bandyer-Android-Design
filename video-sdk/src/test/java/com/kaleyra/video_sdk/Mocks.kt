package com.kaleyra.video_sdk

import android.net.Uri
import com.kaleyra.video.Contact
import com.kaleyra.video.State
import com.kaleyra.video.conference.Call
import com.kaleyra.video.conversation.ChatParticipant
import com.kaleyra.video.conversation.ChatParticipants
import com.kaleyra.video.conversation.Message
import com.kaleyra.video.conversation.OtherMessage
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.ChatUI
import com.kaleyra.video_common_ui.ConferenceUI
import com.kaleyra.video_common_ui.ConversationUI
import com.kaleyra.video_common_ui.MessagesUI
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date

object Mocks {
    val conferenceMock = mockk<ConferenceUI>(relaxed = true)
    val conversationMock = mockk<ConversationUI>(relaxed = true)
    val messagesUIMock = mockk<MessagesUI>(relaxed = true)
    val callMock = mockk<CallUI>(relaxed = true)
    val chatMock = mockk<ChatUI>(relaxed = true)

    val callState = MutableStateFlow<Call.State>(Call.State.Connected)
    val conversationState = MutableStateFlow<State>(State.Connected)
    val otherParticipantState = MutableStateFlow<ChatParticipant.State>(ChatParticipant.State.Invited)
    val otherParticipantState2 = MutableStateFlow<ChatParticipant.State>(ChatParticipant.State.Joined.Online)
    val myParticipantState = MutableStateFlow<ChatParticipant.State>(ChatParticipant.State.Invited)
    val otherParticipantEvents = MutableStateFlow<ChatParticipant.Event>(ChatParticipant.Event.Typing.Idle)
    val otherParticipantEvents2 = MutableStateFlow<ChatParticipant.Event>(ChatParticipant.Event.Typing.Idle)
    val myParticipantEvents = MutableStateFlow<ChatParticipant.Event>(ChatParticipant.Event.Typing.Idle)

    val now: Instant = Instant.now()
    val yesterday: Instant = now.minus(1, ChronoUnit.DAYS)

    val myParticipantMock = spyk(
        object : ChatParticipant.Me {
            override val state: StateFlow<ChatParticipant.State> = myParticipantState
            override val events: StateFlow<ChatParticipant.Event> = myParticipantEvents
            override val userId: String = "myUserId"
            override val restrictions: Contact.Restrictions = mockk()
            override val displayName: StateFlow<String?> = MutableStateFlow(null)
            override val displayImage: StateFlow<Uri?> = MutableStateFlow(null)
            override fun typing() = Unit
        }
    )

    val otherParticipantMock = spyk(
        object : ChatParticipant {
            override val state: StateFlow<ChatParticipant.State> = otherParticipantState
            override val events: StateFlow<ChatParticipant.Event> = otherParticipantEvents
            override val userId: String = "otherUserId"
            override val restrictions: Contact.Restrictions = mockk()
            override val displayName: StateFlow<String?> = MutableStateFlow(null)
            override val displayImage: StateFlow<Uri?> = MutableStateFlow(null)
        }
    )

    val otherParticipantMock2 = spyk(
        object : ChatParticipant {
            override val state: StateFlow<ChatParticipant.State> = otherParticipantState2
            override val events: StateFlow<ChatParticipant.Event> = otherParticipantEvents2
            override val userId: String = "otherUserId2"
            override val restrictions: Contact.Restrictions = mockk()
            override val displayName: StateFlow<String?> = MutableStateFlow(null)
            override val displayImage: StateFlow<Uri?> = MutableStateFlow(null)
        }
    )

    val oneToOneChatParticipantsFlow =
        object : ChatParticipants {
            override val me: ChatParticipant.Me = myParticipantMock
            override val others: List<ChatParticipant> = listOf(otherParticipantMock)
            override val list: List<ChatParticipant> = others + me
            override fun creator(): ChatParticipant? = null
        }

    val groupChatParticipantsMock = object : ChatParticipants {
        override val me: ChatParticipant.Me = myParticipantMock
        override val others: List<ChatParticipant> = listOf(otherParticipantMock, otherParticipantMock2)
        override val list: List<ChatParticipant> = others + me
        override fun creator(): ChatParticipant? = null
    }

    val myMessageMock = object : Message {
        override val id: String = "myId"
        override val creator: ChatParticipant = myParticipantMock
        override val creationDate: Date = Date(now.toEpochMilli())
        override val content: Message.Content = Message.Content.Text("text")
        override val state: StateFlow<Message.State> = MutableStateFlow(Message.State.Read())
    }

    val otherTodayReadMessage = object : OtherMessage {
        override val id: String = "otherId0"
        override val creator: ChatParticipant = otherParticipantMock
        override val creationDate: Date = Date(now.toEpochMilli())
        override val content: Message.Content = Message.Content.Text("otherText")
        override val state: StateFlow<Message.State> = MutableStateFlow(Message.State.Read())
        override fun markAsRead() = Unit
    }

    val otherYesterdayUnreadMessage = spyk(
        object : OtherMessage {
            override val id: String = "otherId1"
            override val creator: ChatParticipant = otherParticipantMock
            override val creationDate: Date = Date(yesterday.toEpochMilli())
            override val content: Message.Content = Message.Content.Text("otherText")
            override val state: StateFlow<Message.State> = MutableStateFlow(Message.State.Received())
            override fun markAsRead() = Unit
        }
    )

    val otherTodayUnreadMessage = spyk(
        object : OtherMessage {
            override val id: String = "otherId2"
            override val creator: ChatParticipant = otherParticipantMock
            override val creationDate: Date = Date(now.toEpochMilli())
            override val content: Message.Content =  Message.Content.Text("otherText2")
            override val state: StateFlow<Message.State> = MutableStateFlow(Message.State.Received())
            override fun markAsRead() = Unit
        }
    )

    val otherTodayUnreadMessage2 = spyk(
        object : OtherMessage {
            override val id: String = "otherId3"
            override val creator: ChatParticipant = otherParticipantMock
            override val creationDate: Date = Date(now.toEpochMilli())
            override val content: Message.Content =  Message.Content.Text("otherText3")
            override val state: StateFlow<Message.State> = MutableStateFlow(Message.State.Received())
            override fun markAsRead() = Unit
        }
    )
}



