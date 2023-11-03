package com.kaleyra.collaboration_suite_phone_ui

import com.kaleyra.collaboration_suite_phone_ui.call.audiooutput.model.AudioOutputUiState
import com.kaleyra.collaboration_suite_phone_ui.call.audiooutput.viewmodel.AudioOutputViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.callactions.model.CallActionsUiState
import com.kaleyra.collaboration_suite_phone_ui.call.callactions.viewmodel.CallActionsViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.dialing.view.DialingUiState
import com.kaleyra.collaboration_suite_phone_ui.call.dialing.viewmodel.DialingViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.ringing.model.RingingUiState
import com.kaleyra.collaboration_suite_phone_ui.call.ringing.viewmodel.RingingViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.screen.model.CallUiState
import com.kaleyra.collaboration_suite_phone_ui.call.screen.viewmodel.CallViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.screenshare.model.ScreenShareUiState
import com.kaleyra.collaboration_suite_phone_ui.call.screenshare.viewmodel.ScreenShareViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.virtualbackground.model.VirtualBackgroundUiState
import com.kaleyra.collaboration_suite_phone_ui.call.virtualbackground.viewmodel.VirtualBackgroundViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.whiteboard.model.WhiteboardUiState
import com.kaleyra.collaboration_suite_phone_ui.call.whiteboard.viewmodel.WhiteboardViewModel
import com.kaleyra.collaboration_suite_phone_ui.common.immutablecollections.ImmutableList
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
                        com.kaleyra.collaboration_suite_phone_ui.call.screenshare.model.ScreenShareTargetUi.Device, com.kaleyra.collaboration_suite_phone_ui.call.screenshare.model.ScreenShareTargetUi.Application)))
                )
            }
        }
        every { AudioOutputViewModel.provideFactory(any()) } returns mockk {
            every { create<AudioOutputViewModel>(any(), any()) } returns mockk(relaxed = true) {
                every { uiState } returns MutableStateFlow(AudioOutputUiState(audioDeviceList = com.kaleyra.collaboration_suite_phone_ui.call.audiooutput.model.mockAudioDevices, playingDeviceId = "id"))
            }
        }
        every { CallActionsViewModel.provideFactory(any()) } returns mockk {
            every { create<CallActionsViewModel>(any(), any()) } returns mockk(relaxed = true) {
               every { uiState } returns MutableStateFlow(CallActionsUiState(actionList = com.kaleyra.collaboration_suite_phone_ui.call.callactions.model.mockCallActions))
            }
        }
        every { WhiteboardViewModel.provideFactory(any(), any()) } returns mockk {
            every { create<WhiteboardViewModel>(any(), any()) } returns mockk(relaxed = true) {
                every { uiState } returns MutableStateFlow(WhiteboardUiState(isLoading = false, isOffline = true))
            }
        }
        every { VirtualBackgroundViewModel.provideFactory(any()) } returns mockk {
            every { create<VirtualBackgroundViewModel>(any(), any()) } returns mockk(relaxed = true) {
                every { uiState } returns MutableStateFlow(VirtualBackgroundUiState(backgroundList = com.kaleyra.collaboration_suite_phone_ui.call.virtualbackground.model.mockVirtualBackgrounds))
            }
        }
    }

    override fun finished(description: Description) {
        unmockkAll()
    }
}