package com.kaleyra.collaboration_suite_phone_ui.chat.conversation.view

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.ConversationElement
import com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.Message
import com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.mock.mockConversationElements
import com.kaleyra.collaboration_suite_phone_ui.chat.conversation.view.item.DayHeaderItem
import com.kaleyra.collaboration_suite_phone_ui.chat.conversation.view.item.MyMessageItem
import com.kaleyra.collaboration_suite_phone_ui.chat.conversation.view.item.NewMessagesHeaderItem
import com.kaleyra.collaboration_suite_phone_ui.chat.conversation.view.item.OtherMessageItem
import com.kaleyra.collaboration_suite_phone_ui.chat.mapper.ParticipantDetails
import com.kaleyra.collaboration_suite_phone_ui.common.avatar.model.ImmutableUri
import com.kaleyra.collaboration_suite_phone_ui.common.immutablecollections.ImmutableList
import com.kaleyra.collaboration_suite_phone_ui.common.immutablecollections.ImmutableMap
import com.kaleyra.collaboration_suite_phone_ui.theme.KaleyraTheme

internal const val MessageStateTag = "MessageStateTag"
internal const val ConversationContentTag = "ConversationContentTag"
internal const val ProgressIndicatorTag = "ProgressIndicatorTag"

val ConversationContentPadding = 16.dp

@Composable
internal fun ConversationContent(
    items: ImmutableList<ConversationElement>,
    participantsDetails: ImmutableMap<String, ParticipantDetails>,
    isFetching: Boolean,
    scrollState: LazyListState,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        reverseLayout = true,
        state = scrollState,
        contentPadding = PaddingValues(all = ConversationContentPadding),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .testTag(ConversationContentTag)
            .then(modifier)
    ) {
        items(items.value, key = { it.id }, contentType = { it::class.java }) { item ->
            when (item) {
                is ConversationElement.Message -> {
                    when (val message = item.message) {
                        is Message.OtherMessage -> OtherMessageItem(
                            message = message,
                            isMessageGroupClosed = item.isMessageGroupClosed,
                            participantDetails = participantsDetails[message.userId],
                            modifier = Modifier.fillMaxWidth()
                        )

                        is Message.MyMessage -> MyMessageItem(
                            message = message,
                            isMessageGroupClosed = item.isMessageGroupClosed,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                is ConversationElement.Day -> DayHeaderItem(
                    timestamp = item.timestamp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )

                is ConversationElement.UnreadMessages -> NewMessagesHeaderItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
            }
        }
        if (isFetching) {
            item {
                CircularProgressIndicator(
                    color = LocalContentColor.current,
                    strokeWidth = 3.dp,
                    modifier = Modifier
                        .padding(16.dp)
                        .size(28.dp)
                        .testTag(ProgressIndicatorTag)
                )
            }
        }
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun ConversationContentPreview() = KaleyraTheme {
    Surface {
        ConversationContent(
            items = mockConversationElements,
            participantsDetails = ImmutableMap(),
            isFetching = false,
            scrollState = rememberLazyListState()
        )
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun ConversationContentGroupPreview() = KaleyraTheme {
    Surface {
        ConversationContent(
            items = mockConversationElements,
            participantsDetails = ImmutableMap(
                hashMapOf(
                    "userId1" to ParticipantDetails("Enea", ImmutableUri()),
                    "userId4" to ParticipantDetails("Luca", ImmutableUri()),
                    "userId5" to ParticipantDetails("Franco", ImmutableUri()),
                    "userId7" to ParticipantDetails("Francesco", ImmutableUri()),
                    "userId8" to ParticipantDetails("Marco", ImmutableUri()),
                )
            ),
            isFetching = false,
            scrollState = rememberLazyListState()
        )
    }
}











