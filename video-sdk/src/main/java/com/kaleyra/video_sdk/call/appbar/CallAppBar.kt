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

package com.kaleyra.video_sdk.call.appbar

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.common.button.IconButton
import com.kaleyra.video_sdk.common.topappbar.TopAppBar
import com.kaleyra.video_sdk.R

@Composable
internal fun CallAppBar(
    title: String,
    onBackPressed: () -> Unit,
    actions: @Composable (RowScope.() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        navigationIcon = {
            IconButton(
                icon = painterResource(id = R.drawable.ic_kaleyra_back_down),
                iconDescription = stringResource(id = R.string.kaleyra_close),
                onClick = onBackPressed,
                modifier = Modifier.padding(4.dp)
            )
        },
        content = {
            Text(
                text = title,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth()
            )
        },
        actions = {
            if (actions != null) {
                actions()
            }
        },
        elevation = 0.dp,
        contentPadding = WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal).asPaddingValues(),
        modifier = modifier
    )
}