package com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.mock

import com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.ConversationElement
import com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.Message
import com.kaleyra.collaboration_suite_phone_ui.common.immutablecollections.ImmutableList
import kotlinx.coroutines.flow.MutableStateFlow

val mockConversationElements = ImmutableList(
    listOf(
        ConversationElement.Message(
            Message.OtherMessage(
                "id8",
                "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.",
                "15:01",
                "userId8"
            ),
            isMessageGroupClosed = true
        ),
        ConversationElement.Message(
            Message.OtherMessage(
                "id7",
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
                "15:00",
                "userId7"
            ),
            isMessageGroupClosed = true
        ),
        ConversationElement.UnreadMessages,
        ConversationElement.Message(
            Message.MyMessage(
                "userId6",
                "Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.",
                "13:18",
                MutableStateFlow(Message.State.Sent)
            ),
            isMessageGroupClosed = true
        ),
        ConversationElement.Message(
            Message.OtherMessage(
                "id5",
                "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur..",
                "13:15",
                "userId5"
            ),
            isMessageGroupClosed = true
        ),
        ConversationElement.Message(
            Message.OtherMessage(
                "id4",
                "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.",
                "13:12",
                "userId4"
            ),
            isMessageGroupClosed = true
        ),
        ConversationElement.Message(
            Message.MyMessage(
                "id3",
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
                "13:00",
                MutableStateFlow(Message.State.Read)
            ),
            isMessageGroupClosed = true
        ),
        ConversationElement.Message(
            Message.MyMessage(
                "id2",
                "How is going?",
                "11:55",
                MutableStateFlow(Message.State.Read)
            ),
            isMessageGroupClosed = true
        ),
        ConversationElement.Message(Message.OtherMessage("id1", "Hello there!", "11:45", "userId1"), isMessageGroupClosed = true),
        ConversationElement.Day(92209343)
    )
)