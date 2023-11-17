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

package com.kaleyra.video_sdk.call.screen.model

import androidx.compose.runtime.Immutable
import com.kaleyra.video_sdk.call.stream.model.StreamUi
import com.kaleyra.video_sdk.common.uistate.UiState
import com.kaleyra.video_sdk.call.recording.model.RecordingUi
import com.kaleyra.video_sdk.call.callinfowidget.model.WatermarkInfo
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList

@Immutable
data class CallUiState(
    val callState: CallStateUi = CallStateUi.Disconnected,
    val thumbnailStreams: ImmutableList<StreamUi> = ImmutableList(listOf()),
    val featuredStreams: ImmutableList<StreamUi> = ImmutableList(listOf()),
    val fullscreenStream: StreamUi? = null,
    val watermarkInfo: WatermarkInfo? = null,
    val recording: RecordingUi? = null,
    val isAudioOnly: Boolean = false,
    val isGroupCall: Boolean = false,
    val amILeftAlone: Boolean = false,
    val showFeedback: Boolean = false,
    val shouldAutoHideSheet: Boolean = false
) : UiState