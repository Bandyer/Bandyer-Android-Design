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

package com.kaleyra.video_sdk.call.screenshare

import android.content.res.Configuration
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.kaleyra.video_sdk.call.subfeaturelayout.SubFeatureLayout
import com.kaleyra.video_sdk.call.screenshare.model.ScreenShareTargetUi
import com.kaleyra.video_sdk.call.screenshare.model.ScreenShareUiState
import com.kaleyra.video_sdk.call.screenshare.view.ScreenShareContent
import com.kaleyra.video_sdk.call.screenshare.viewmodel.ScreenShareViewModel
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.theme.KaleyraTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaleyra.video_common_ui.requestConfiguration
import com.kaleyra.video_sdk.R

@Composable
internal fun ScreenShareComponent(
    viewModel: ScreenShareViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = ScreenShareViewModel.provideFactory(::requestConfiguration)
    ),
    onItemClick: (ScreenShareTargetUi) -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val onClick = remember {
        { target: ScreenShareTargetUi ->
            when (target) {
                ScreenShareTargetUi.Application -> viewModel.shareApplicationScreen(context)
                ScreenShareTargetUi.Device -> viewModel.shareDeviceScreen(context)
            }
            onItemClick(target)
        }
    }
    ScreenShareComponent(
        uiState = uiState,
        onItemClick = onClick,
        onCloseClick = onCloseClick,
        modifier = modifier
    )
}

@Composable
internal fun ScreenShareComponent(
    uiState: ScreenShareUiState,
    onItemClick: (ScreenShareTargetUi) -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SubFeatureLayout(
        title = stringResource(id = R.string.kaleyra_screenshare_picker_title),
        onCloseClick = onCloseClick,
        modifier = modifier
    ) {
        ScreenShareContent(
            items = uiState.targetList,
            onItemClick = onItemClick
        )
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun ScreenShareComponentPreview() {
    KaleyraTheme {
        Surface {
            ScreenShareComponent(
                uiState = ScreenShareUiState(targetList = ImmutableList(listOf(ScreenShareTargetUi.Device, ScreenShareTargetUi.Application))),
                onItemClick = { },
                onCloseClick = { }
            )
        }
    }
}