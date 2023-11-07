package com.kaleyra.video_sdk.chat.conversation.view

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
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
import com.kaleyra.video_sdk.chat.appbar.model.ChatParticipantDetails
import com.kaleyra.video_sdk.chat.conversation.model.ConversationItem
import com.kaleyra.video_sdk.chat.conversation.model.Message
import com.kaleyra.video_sdk.chat.conversation.model.mock.mockConversationElements
import com.kaleyra.video_sdk.chat.conversation.view.item.DayHeaderItem
import com.kaleyra.video_sdk.chat.conversation.view.item.MyMessageItem
import com.kaleyra.video_sdk.chat.conversation.view.item.NewMessagesHeaderItem
import com.kaleyra.video_sdk.chat.conversation.view.item.OtherMessageItem
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableMap
import com.kaleyra.video_sdk.theme.KaleyraTheme

internal const val MessageStateTag = "MessageStateTag"
internal const val ConversationContentTag = "ConversationContentTag"
internal const val ProgressIndicatorTag = "ProgressIndicatorTag"

val ConversationContentPadding = 16.dp

@Composable
internal fun ConversationContent(
    items: ImmutableList<ConversationItem>,
    participantsDetails: ImmutableMap<String, ChatParticipantDetails>?,
    isFetching: Boolean,
    scrollState: LazyListState,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        reverseLayout = true,
        state = scrollState,
        contentPadding = PaddingValues(all = ConversationContentPadding),
        verticalArrangement = Arrangement.spacedBy(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .testTag(ConversationContentTag)
            .then(modifier)
    ) {
        items(items.value, key = { it.id }, contentType = { it::class.java }) { item ->
            when (item) {
                is ConversationItem.Message -> {
                    when (val message = item.message) {
                        is Message.OtherMessage -> OtherMessageItem(
                            message = message,
                            isFirstChainMessage = item.isFirstChainMessage,
                            isLastChainMessage = item.isLastChainMessage,
                            participantDetails = participantsDetails?.get(message.userId),
                            modifier = Modifier.fillMaxWidth()
                        )

                        is Message.MyMessage -> MyMessageItem(
                            message = message,
                            isFirstChainMessage = item.isFirstChainMessage,
                            isLastChainMessage = item.isLastChainMessage,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                is ConversationItem.Day -> DayHeaderItem(
                    timestamp = item.timestamp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                )

                is ConversationItem.UnreadMessages -> NewMessagesHeaderItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
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
                    "userId1" to ChatParticipantDetails("Enea"),
                    "userId4" to ChatParticipantDetails("Luca"),
                    "userId5" to ChatParticipantDetails("Franco"),
                    "userId7" to ChatParticipantDetails("Francesco"),
                    "userId8" to ChatParticipantDetails("Marco"),
                )
            ),
            isFetching = false,
            scrollState = rememberLazyListState()
        )
    }
}











