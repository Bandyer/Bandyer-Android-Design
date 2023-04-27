package com.kaleyra.collaboration_suite_phone_ui.call.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList

const val ThumbnailStreamsTag = "ThumbnailStreamsTag"

@Composable
internal fun ThumbnailStreams(
    streams: ImmutableList<StreamUi>,
    onStreamClick: (StreamUi) -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    modifier: Modifier = Modifier
) {
    LazyRow(
        contentPadding = contentPadding,
        reverseLayout = true,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.testTag(ThumbnailStreamsTag)
    ) {
        items(items = streams.value, key = { it.id }) {
            ThumbnailStream(it, onClick = { onStreamClick(it) })
        }
    }
}

