package com.kaleyra.collaboration_suite_phone_ui.chat.conversation.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.ConversationElement
import com.kaleyra.collaboration_suite_phone_ui.chat.conversation.view.item.DayHeaderItem
import com.kaleyra.collaboration_suite_phone_ui.chat.conversation.view.item.MessageItem
import com.kaleyra.collaboration_suite_phone_ui.chat.conversation.view.item.NewMessagesHeaderItem
import com.kaleyra.collaboration_suite_phone_ui.common.immutablecollections.ImmutableList

internal const val MessageStateTag = "MessageStateTag"
internal const val ConversationContentTag = "ConversationContentTag"
internal const val ProgressIndicatorTag = "ProgressIndicatorTag"

@Composable
internal fun ConversationContent(
    items: ImmutableList<ConversationElement>,
    isFetching: Boolean,
    scrollState: LazyListState,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        reverseLayout = true,
        state = scrollState,
        contentPadding = PaddingValues(all = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .testTag(ConversationContentTag)
            .then(modifier)
    ) {
        items(items.value, key = { it.id }, contentType = { it::class.java }) { item ->
            when (item) {
                is ConversationElement.Message -> MessageItem(
                    message = item,
                    modifier = Modifier.fillMaxWidth()
                )
                is ConversationElement.Day -> DayHeaderItem(
                    timestamp = item.timestamp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
                is ConversationElement.UnreadMessages -> NewMessagesHeaderItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
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











