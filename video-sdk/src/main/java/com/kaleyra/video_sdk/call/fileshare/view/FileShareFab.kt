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

package com.kaleyra.video_sdk.call.fileshare.view

import android.content.res.Configuration
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.theme.KaleyraTheme
import com.kaleyra.video_sdk.R

private val FabSize = 56.dp
private val FabIconPadding = 16.dp
private val FabPadding = 20.dp

@Composable
internal fun FileShareFab(
    collapsed: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FloatingActionButton(
        modifier = modifier.sizeIn(minWidth = FabSize, minHeight = FabSize),
        onClick = onClick,
        contentColor = MaterialTheme.colors.surface
    ) {
        val padding = if (collapsed) 0.dp else FabPadding
        Row(
            modifier = Modifier
                .padding(horizontal = padding)
                .animateContentSize(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_kaleyra_add),
                contentDescription = stringResource(id = R.string.kaleyra_fileshare_add_description)
            )
            if (!collapsed) {
                Spacer(Modifier.width(FabIconPadding))
                Text(
                    text = stringResource(id = R.string.kaleyra_fileshare_add).uppercase(),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun FileShareFabPreview() {
    KaleyraTheme {
        FileShareFab(
            collapsed = false,
            onClick = { }
        )
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun FileShareFabCollapsedPreview() {
    KaleyraTheme {
        FileShareFab(
            collapsed = true,
            onClick = { }
        )
    }
}