package com.kaleyra.collaboration_suite_phone_ui

import com.kaleyra.collaboration_suite_phone_ui.call.compose.CallUiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.CallViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.compose.audiooutput.model.AudioOutputUiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.audiooutput.model.mockAudioDevices
import com.kaleyra.collaboration_suite_phone_ui.call.compose.audiooutput.viewmodel.AudioOutputViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.model.CallActionsUiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.model.mockCallActions
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.viewmodel.CallActionsViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.compose.dialing.view.DialingUiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.dialing.viewmodel.DialingViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.compose.precall.model.PreCallUiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.precall.viewmodel.PreCallViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.compose.ringing.model.RingingUiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.ringing.viewmodel.RingingViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.compose.screenshare.model.ScreenShareTargetUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.screenshare.model.ScreenShareUiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.screenshare.viewmodel.ScreenShareViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.compose.virtualbackground.model.VirtualBackgroundUiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.virtualbackground.model.mockVirtualBackgrounds
import com.kaleyra.collaboration_suite_phone_ui.call.compose.virtualbackground.viewmodel.VirtualBackgroundViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.model.WhiteboardUiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.viewmodel.WhiteboardViewModel
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
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
                every { uiState } returns MutableStateFlow(ScreenShareUiState(targetList = ImmutableList(listOf(ScreenShareTargetUi.Device, ScreenShareTargetUi.Application))))
            }
        }
        every { AudioOutputViewModel.provideFactory(any()) } returns mockk {
            every { create<AudioOutputViewModel>(any(), any()) } returns mockk(relaxed = true) {
                every { uiState } returns MutableStateFlow(AudioOutputUiState(audioDeviceList = mockAudioDevices, playingDeviceId = "id"))
            }
        }
        every { CallActionsViewModel.provideFactory(any()) } returns mockk {
            every { create<CallActionsViewModel>(any(), any()) } returns mockk(relaxed = true) {
               every { uiState } returns MutableStateFlow(CallActionsUiState(actionList = mockCallActions))
            }
        }
        every { WhiteboardViewModel.provideFactory(any(), any()) } returns mockk {
            every { create<WhiteboardViewModel>(any(), any()) } returns mockk(relaxed = true) {
                every { uiState } returns MutableStateFlow(WhiteboardUiState(isLoading = false, isOffline = true))
            }
        }
        every { VirtualBackgroundViewModel.provideFactory(any()) } returns mockk {
            every { create<VirtualBackgroundViewModel>(any(), any()) } returns mockk(relaxed = true) {
                every { uiState } returns MutableStateFlow(VirtualBackgroundUiState(backgroundList = mockVirtualBackgrounds))
            }
        }
    }

    override fun finished(description: Description) {
        unmockkAll()
    }
}