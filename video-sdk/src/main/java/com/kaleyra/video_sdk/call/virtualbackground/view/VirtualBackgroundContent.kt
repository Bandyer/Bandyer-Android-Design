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

package com.kaleyra.video_sdk.call.virtualbackground.view

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.call.virtualbackground.model.VirtualBackgroundUi
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.theme.KaleyraTheme

@Composable
internal fun VirtualBackgroundContent(
    items: ImmutableList<VirtualBackgroundUi>,
    currentBackground: VirtualBackgroundUi,
    onItemClick: (VirtualBackgroundUi) -> Unit
) {
    LazyColumn {
        items(items = items.value.distinctBy { it.id }, key = { it.id }) {
            VirtualBackgroundItem(
                background = it,
                selected = it == currentBackground,
                modifier = Modifier
                    .clickable(
                        onClickLabel = clickLabelFor(it),
                        role = Role.Button,
                        onClick = { onItemClick(it) }
                    )
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            )
        }
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun ScreenShareContentPreview() {
    KaleyraTheme {
        Surface {
            VirtualBackgroundContent(
                items = ImmutableList(listOf(VirtualBackgroundUi.None, VirtualBackgroundUi.Blur(id = "id"), VirtualBackgroundUi.Image(id = "id2"))),
                currentBackground = VirtualBackgroundUi.Blur(id = "id"),
                onItemClick = { }
            )
        }
    }
}