package com.kaleyra.video_sdk.chat.conversation.model.mock

import com.kaleyra.video_sdk.chat.conversation.model.ConversationItem
import com.kaleyra.video_sdk.chat.conversation.model.Message
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import kotlinx.coroutines.flow.MutableStateFlow

val mockConversationElements = ImmutableList(
    listOf(
        ConversationItem.Message(
            Message.OtherMessage(
                "id9",
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
                "15:02",
                "userId8"
            ),
            isFirstChainMessage = true,
            isLastChainMessage = true
        ),
        ConversationItem.Message(
            Message.OtherMessage(
                "id8",
                "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.",
                "15:01",
                "userId7"
            ),
            isFirstChainMessage = false,
            isLastChainMessage = true
        ),
        ConversationItem.Message(
            Message.OtherMessage(
                "id7",
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
                "15:00",
                "userId7"
            ),
            isFirstChainMessage = true,
            isLastChainMessage = false
        ),
        ConversationItem.UnreadMessages,
        ConversationItem.Message(
            Message.MyMessage(
                "userId6",
                "Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.",
                "13:18",
                MutableStateFlow(Message.State.Sent)
            ),
            isFirstChainMessage = true,
            isLastChainMessage = true
        ),
        ConversationItem.Message(
            Message.OtherMessage(
                "id5",
                "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur..",
                "13:15",
                "userId5"
            ),
            isFirstChainMessage = true,
            isLastChainMessage = true
        ),
        ConversationItem.Message(
            Message.OtherMessage(
                "id4",
                "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.",
                "13:12",
                "userId4"
            ),
            isFirstChainMessage = true,
            isLastChainMessage = true
        ),
        ConversationItem.Message(
            Message.MyMessage(
                "id3",
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
                "13:00",
                MutableStateFlow(Message.State.Read)
            ),
            isFirstChainMessage = false,
            isLastChainMessage = true
        ),
        ConversationItem.Message(
            Message.MyMessage(
                "id2",
                "How is going?",
                "11:55",
                MutableStateFlow(Message.State.Read)
            ),
            isFirstChainMessage = true,
            isLastChainMessage = false
        ),
        ConversationItem.Message(Message.OtherMessage("id1", "Hello there!", "11:45", "userId1"), isFirstChainMessage = true, isLastChainMessage = true),
        ConversationItem.Day(92209343)
    )
)