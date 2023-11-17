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

package com.kaleyra.video_sdk

import com.kaleyra.video_sdk.call.audiooutput.model.AudioOutputUiState
import com.kaleyra.video_sdk.call.audiooutput.viewmodel.AudioOutputViewModel
import com.kaleyra.video_sdk.call.callactions.model.CallActionsUiState
import com.kaleyra.video_sdk.call.callactions.viewmodel.CallActionsViewModel
import com.kaleyra.video_sdk.call.dialing.view.DialingUiState
import com.kaleyra.video_sdk.call.dialing.viewmodel.DialingViewModel
import com.kaleyra.video_sdk.call.ringing.model.RingingUiState
import com.kaleyra.video_sdk.call.ringing.viewmodel.RingingViewModel
import com.kaleyra.video_sdk.call.screen.model.CallUiState
import com.kaleyra.video_sdk.call.screen.viewmodel.CallViewModel
import com.kaleyra.video_sdk.call.screenshare.model.ScreenShareUiState
import com.kaleyra.video_sdk.call.screenshare.viewmodel.ScreenShareViewModel
import com.kaleyra.video_sdk.call.virtualbackground.model.VirtualBackgroundUiState
import com.kaleyra.video_sdk.call.virtualbackground.viewmodel.VirtualBackgroundViewModel
import com.kaleyra.video_sdk.call.whiteboard.model.WhiteboardUiState
import com.kaleyra.video_sdk.call.whiteboard.viewmodel.WhiteboardViewModel
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.rules.TestWatcher
import org.junit.runner.Description

class MockCallViewModelsStatesRule : TestWatcher() {
    override fun starting(description: Description) {
        mockkObject(CallViewModel)
        mockkObject(DialingViewModel)
        mockkObject(RingingViewModel)
        mockkObject(ScreenShareViewModel)
        mockkObject(AudioOutputViewModel)
        mockkObject(CallActionsViewModel)
        mockkObject(WhiteboardViewModel)
        mockkObject(VirtualBackgroundViewModel)
        every { CallViewModel.provideFactory(any()) } returns mockk {
            every { create<CallViewModel>(any(), any()) } returns mockk(relaxed = true) {
                every { uiState } returns MutableStateFlow(CallUiState())
            }
        }
        every { DialingViewModel.provideFactory(any()) } returns mockk {
            every { create<DialingViewModel>(any(), any()) } returns mockk(relaxed = true) {
                every { uiState } returns MutableStateFlow(DialingUiState())
            }
        }
        every { RingingViewModel.provideFactory(any()) } returns mockk {
            every { create<RingingViewModel>(any(), any()) } returns mockk(relaxed = true) {
                every { uiState } returns MutableStateFlow(RingingUiState())
            }
        }
        every { ScreenShareViewModel.provideFactory(any()) } returns mockk {
            every { create<ScreenShareViewModel>(any(), any()) } returns mockk(relaxed = true) {
                every { uiState } returns MutableStateFlow(
                    ScreenShareUiState(targetList = ImmutableList(listOf(
                        com.kaleyra.video_sdk.call.screenshare.model.ScreenShareTargetUi.Device, com.kaleyra.video_sdk.call.screenshare.model.ScreenShareTargetUi.Application)))
                )
            }
        }
        every { AudioOutputViewModel.provideFactory(any()) } returns mockk {
            every { create<AudioOutputViewModel>(any(), any()) } returns mockk(relaxed = true) {
                every { uiState } returns MutableStateFlow(AudioOutputUiState(audioDeviceList = com.kaleyra.video_sdk.call.audiooutput.model.mockAudioDevices, playingDeviceId = "id"))
            }
        }
        every { CallActionsViewModel.provideFactory(any()) } returns mockk {
            every { create<CallActionsViewModel>(any(), any()) } returns mockk(relaxed = true) {
               every { uiState } returns MutableStateFlow(CallActionsUiState(actionList = com.kaleyra.video_sdk.call.callactions.model.mockCallActions))
            }
        }
        every { WhiteboardViewModel.provideFactory(any(), any()) } returns mockk {
            every { create<WhiteboardViewModel>(any(), any()) } returns mockk(relaxed = true) {
                every { uiState } returns MutableStateFlow(WhiteboardUiState(isLoading = false, isOffline = true))
            }
        }
        every { VirtualBackgroundViewModel.provideFactory(any()) } returns mockk {
            every { create<VirtualBackgroundViewModel>(any(), any()) } returns mockk(relaxed = true) {
                every { uiState } returns MutableStateFlow(VirtualBackgroundUiState(backgroundList = com.kaleyra.video_sdk.call.virtualbackground.model.mockVirtualBackgrounds))
            }
        }
    }

    override fun finished(description: Description) {
        unmockkAll()
    }
}