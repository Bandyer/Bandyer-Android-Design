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

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.call.appbar.CallAppBar
import com.kaleyra.video_sdk.theme.KaleyraTheme
import com.kaleyra.video_sdk.R

@Composable
internal fun FileShareAppBar(onBackPressed: () -> Unit, modifier: Modifier = Modifier) {
    CallAppBar(
        onBackPressed = onBackPressed,
        title = stringResource(id = R.string.kaleyra_fileshare),
        actions = { Spacer(Modifier.width(56.dp)) },
        modifier = modifier
    )
}

@Preview
@Composable
internal fun FileShareAppBarTest() {
    KaleyraTheme {
        FileShareAppBar(onBackPressed = { })
    }
}