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

package com.kaleyra.video_sdk.call.dialing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kaleyra.video_sdk.call.dialing.view.DialingUiState
import com.kaleyra.video_sdk.call.dialing.viewmodel.DialingViewModel
import com.kaleyra.video_sdk.call.precall.PreCallComponent
import com.kaleyra.video_sdk.common.usermessages.model.UserMessage
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.theme.KaleyraTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaleyra.video_common_ui.requestConfiguration
import com.kaleyra.video_sdk.R

const val DialingContentTag = "DialingContentTag"

@Composable
internal fun DialingComponent(
    modifier: Modifier = Modifier,
    viewModel: DialingViewModel = viewModel(
        factory = DialingViewModel.provideFactory(::requestConfiguration)
    ),
    onBackPressed: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle(null)

    DialingComponent(
        uiState = uiState,
        userMessage = userMessage,
        onBackPressed = onBackPressed,
        modifier = modifier
    )
}

@Composable
internal fun DialingComponent(
    uiState: DialingUiState,
    modifier: Modifier = Modifier,
    userMessage: UserMessage? = null,
    onBackPressed: () -> Unit = { }
) {
    PreCallComponent(
        uiState = uiState,
        userMessage = userMessage,
        subtitle = stringResource(id = R.string.kaleyra_call_status_ringing),
        onBackPressed = onBackPressed,
        modifier = modifier.testTag(DialingContentTag)
    )
}

@Preview
@Composable
fun DialingComponentPreview() {
    KaleyraTheme {
        DialingComponent(
            uiState = DialingUiState(participants = ImmutableList(listOf("user1", "user2"))),
            onBackPressed = { }
        )
    }
}