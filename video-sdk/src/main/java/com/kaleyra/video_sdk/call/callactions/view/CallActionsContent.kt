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

package com.kaleyra.video_sdk.call.callactions.view

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.call.callactions.model.CallAction
import com.kaleyra.video_sdk.call.callactions.model.mockCallActions
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.theme.KaleyraTheme
import com.kaleyra.video_sdk.extensions.ModifierExtensions.fadeBelowOfRootBottomBound

@Composable
internal fun CallActionsContent(
    items: ImmutableList<CallAction>,
    itemsPerRow: Int,
    onItemClick: (action: CallAction) -> Unit,
    isDarkTheme: Boolean = false,
    enableBottomFade: Boolean = true,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(modifier = modifier, columns = GridCells.Fixed(count = itemsPerRow)) {
        items(items = items.value, key = { it::class.java.name }) { action ->
            CallAction(
                action = action,
                isDarkTheme = isDarkTheme,
                onToggle = { onItemClick(action) },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 20.dp, bottom = 8.dp)
                    .then(if (enableBottomFade) Modifier.fadeBelowOfRootBottomBound() else Modifier)
            )
        }
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun CallActionsContentPreview() {
    KaleyraTheme {
        Surface {
            CallActionsContent(
                items = mockCallActions,
                itemsPerRow = 4,
                onItemClick = { },
                enableBottomFade = false
            )
        }
    }
}