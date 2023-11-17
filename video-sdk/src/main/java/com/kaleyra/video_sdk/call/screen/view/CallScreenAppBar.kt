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

package com.kaleyra.video_sdk.call.screen.view

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.kaleyra.video_sdk.call.bottomsheet.BottomSheetComponent
import com.kaleyra.video_sdk.call.fileshare.view.FileShareAppBar
import com.kaleyra.video_sdk.call.whiteboard.view.WhiteboardAppBar
import com.kaleyra.video_sdk.common.spacer.StatusBarsSpacer

const val CallScreenAppBarTag = "CallScreenAppBarTag"

@Composable
internal fun CallScreenAppBar(
    currentSheetComponent: BottomSheetComponent,
    visible: Boolean,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically(),
        modifier = modifier.testTag(CallScreenAppBarTag)
    ) {
        Surface(elevation = AppBarDefaults.TopAppBarElevation) {
            StatusBarsSpacer(Modifier.background(MaterialTheme.colors.primary))
            Box(modifier = Modifier.statusBarsPadding()) {
                when (currentSheetComponent) {
                    BottomSheetComponent.FileShare -> FileShareAppBar(onBackPressed = onBackPressed)
                    BottomSheetComponent.Whiteboard -> WhiteboardAppBar(onBackPressed = onBackPressed)
                    else -> Unit
                }
            }
        }
    }
}

