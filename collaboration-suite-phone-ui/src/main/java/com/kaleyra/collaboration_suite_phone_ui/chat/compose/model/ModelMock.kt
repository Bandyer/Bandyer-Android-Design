package com.kaleyra.collaboration_suite_phone_ui.chat.compose.model

import com.kaleyra.collaboration_suite_phone_ui.chat.compose.topappbar.ClickableAction
import kotlinx.coroutines.flow.MutableStateFlow

val mockConversationItems = listOf(
    ConversationItem.MessageItem(
        Message.OtherMessage(
            "id8",
            "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.",
            "15:01"
        )
    ),
    ConversationItem.MessageItem(
        Message.OtherMessage(
            "id7",
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
            "15:00"
        )
    ),
    ConversationItem.NewMessagesItem(2),
    ConversationItem.MessageItem(
        Message.MyMessage(
            "id6",
            "Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.",
            "13:18",
            MutableStateFlow(
                Message.State.Read
            )
        )
    ),
    ConversationItem.MessageItem(
        Message.OtherMessage(
            "id5",
            "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur..",
            "13:15"
        )
    ),
    ConversationItem.MessageItem(
        Message.OtherMessage(
            "id4",
            "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.",
            "13:12"
        )
    ),
    ConversationItem.MessageItem(
        Message.MyMessage(
            "id3",
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
            "13:00",
            MutableStateFlow(
                Message.State.Read
            )
        )
    ),
    ConversationItem.MessageItem(
        Message.MyMessage(
            "id2", "How is going?", "11:55", MutableStateFlow(
                Message.State.Read
            )
        )
    ),
    ConversationItem.MessageItem(Message.OtherMessage("id1", "Hello there!", "11:45")),
    ConversationItem.DayItem("23 august 2022")
)

val mockClickableActions = setOf(
    ClickableAction(ChatAction.AudioCall) { },
    ClickableAction(ChatAction.AudioUpgradableCall) { },
    ClickableAction(ChatAction.VideoCall) { })