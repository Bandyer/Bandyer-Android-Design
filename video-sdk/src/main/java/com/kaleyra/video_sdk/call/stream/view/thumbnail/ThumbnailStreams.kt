/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.video_sdk.call.stream.view.thumbnail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.call.stream.model.StreamUi
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList

const val ThumbnailStreamsTag = "ThumbnailStreamsTag"

@Composable
internal fun ThumbnailStreams(
    streams: ImmutableList<StreamUi>,
    onStreamClick: (String) -> Unit,
    onStreamDoubleClick: (String) -> Unit,
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
            ThumbnailStream(it, onClick = { onStreamClick(it.id) }, onDoubleClick = { onStreamDoubleClick(it.id) })
        }
    }
}

