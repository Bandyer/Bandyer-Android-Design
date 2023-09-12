package com.kaleyra.collaboration_suite_phone_ui.ui

import androidx.lifecycle.ViewModelProvider
import com.kaleyra.collaboration_suite_core_ui.KaleyraVideoService
import com.kaleyra.collaboration_suite_phone_ui.call.component.screen.model.CallUiState
import com.kaleyra.collaboration_suite_phone_ui.call.component.screen.viewmodel.CallViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.component.audiooutput.model.AudioOutputUiState
import com.kaleyra.collaboration_suite_phone_ui.call.component.audiooutput.model.mockAudioDevices
import com.kaleyra.collaboration_suite_phone_ui.call.component.audiooutput.viewmodel.AudioOutputViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.component.callactions.model.CallActionsUiState
import com.kaleyra.collaboration_suite_phone_ui.call.component.callactions.model.mockCallActions
import com.kaleyra.collaboration_suite_phone_ui.call.component.callactions.viewmodel.CallActionsViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.component.dialing.view.DialingUiState
import com.kaleyra.collaboration_suite_phone_ui.call.component.dialing.viewmodel.DialingViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.component.ringing.model.RingingUiState
import com.kaleyra.collaboration_suite_phone_ui.call.component.ringing.viewmodel.RingingViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.component.screenshare.model.ScreenShareTargetUi
import com.kaleyra.collaboration_suite_phone_ui.call.component.screenshare.model.ScreenShareUiState
import com.kaleyra.collaboration_suite_phone_ui.call.component.screenshare.viewmodel.ScreenShareViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.component.virtualbackground.model.VirtualBackgroundUiState
import com.kaleyra.collaboration_suite_phone_ui.call.component.virtualbackground.model.mockVirtualBackgrounds
import com.kaleyra.collaboration_suite_phone_ui.call.component.virtualbackground.viewmodel.VirtualBackgroundViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.component.whiteboard.model.WhiteboardUiState
import com.kaleyra.collaboration_suite_phone_ui.call.component.whiteboard.viewmodel.WhiteboardViewModel
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.AfterClass
import org.junit.BeforeClass

abstract class ComposeViewModelsMockTest {

    companion object {

        private val callViewModel = mockk<CallViewModel>(relaxed = true)

        private val dialingViewModel = mockk<DialingViewModel>(relaxed = true)

        private val ringingViewModel = mockk<RingingViewModel>(relaxed = true)

        private val screenShareViewModel = mockk<ScreenShareViewModel>(relaxed = true)

        private val audioOutputViewModel = mockk<AudioOutputViewModel>(relaxed = true)

        private val callActionsViewModel = mockk<CallActionsViewModel>(relaxed = true)

        private val whiteboardViewModel = mockk<WhiteboardViewModel>(relaxed = true)

        private val virtualBackgroundViewModel = mockk<VirtualBackgroundViewModel>(relaxed = true)

        private val callViewModelFactory = mockk<ViewModelProvider.Factory>(relaxed = true)

        private val dialingViewModelFactory = mockk<ViewModelProvider.Factory>(relaxed = true)

        private val ringingViewModelFactory = mockk<ViewModelProvider.Factory>(relaxed = true)

        private val screenShareViewModelFactory = mockk<ViewModelProvider.Factory>(relaxed = true)

        private val audioOutputViewModelFactory = mockk<ViewModelProvider.Factory>(relaxed = true)

        private val callActionsViewModelFactory = mockk<ViewModelProvider.Factory>(relaxed = true)

        private val whiteboardViewModelFactory = mockk<ViewModelProvider.Factory>(relaxed = true)

        private val virtualBackgroundViewModelFactory = mockk<ViewModelProvider.Factory>(relaxed = true)

        @BeforeClass
        @JvmStatic
        fun setup() {
            mockkObject(KaleyraVideoService)
            mockkObject(CallViewModel)
            mockkObject(DialingViewModel)
            mockkObject(RingingViewModel)
            mockkObject(ScreenShareViewModel)
            mockkObject(AudioOutputViewModel)
            mockkObject(CallActionsViewModel)
            mockkObject(WhiteboardViewModel)
            mockkObject(VirtualBackgroundViewModel)

            coEvery { KaleyraVideoService.get() } returns mockk(relaxed = true)

            every { CallViewModel.provideFactory(any()) } returns callViewModelFactory
            every { callViewModelFactory.create<CallViewModel>(any(), any()) } returns callViewModel
            every { callViewModel.uiState } returns MutableStateFlow(CallUiState())

            every { DialingViewModel.provideFactory(any()) } returns dialingViewModelFactory
            every { dialingViewModelFactory.create<DialingViewModel>(any(), any()) } returns dialingViewModel
            every { dialingViewModel.uiState } returns MutableStateFlow(DialingUiState())

            every { RingingViewModel.provideFactory(any()) } returns ringingViewModelFactory
            every { ringingViewModelFactory.create<RingingViewModel>(any(), any()) } returns ringingViewModel
            every { ringingViewModel.uiState } returns MutableStateFlow(RingingUiState())

            every { ScreenShareViewModel.provideFactory(any()) } returns screenShareViewModelFactory
            every { screenShareViewModelFactory.create<ScreenShareViewModel>(any(), any()) } returns screenShareViewModel
            every { screenShareViewModel.uiState } returns MutableStateFlow( ScreenShareUiState(targetList = ImmutableList(listOf(
                ScreenShareTargetUi.Device, ScreenShareTargetUi.Application)))
            )

            every { AudioOutputViewModel.provideFactory(any()) } returns audioOutputViewModelFactory
            every { audioOutputViewModelFactory.create<AudioOutputViewModel>(any(), any()) } returns audioOutputViewModel
            every { audioOutputViewModel.uiState } returns MutableStateFlow(AudioOutputUiState(audioDeviceList = mockAudioDevices, playingDeviceId = "id"))

            every { CallActionsViewModel.provideFactory(any()) } returns callActionsViewModelFactory
            every { callActionsViewModelFactory.create<CallActionsViewModel>(any(), any()) } returns callActionsViewModel
            every { callActionsViewModel.uiState } returns MutableStateFlow(CallActionsUiState(actionList = mockCallActions))

            every { WhiteboardViewModel.provideFactory(any(), any()) } returns whiteboardViewModelFactory
            every { whiteboardViewModelFactory.create<WhiteboardViewModel>(any(), any()) } returns whiteboardViewModel
            every { whiteboardViewModel.uiState } returns MutableStateFlow(WhiteboardUiState(isLoading = false, isOffline = true))

            every { VirtualBackgroundViewModel.provideFactory(any()) } returns virtualBackgroundViewModelFactory
            every { virtualBackgroundViewModelFactory.create<VirtualBackgroundViewModel>(any(), any()) } returns virtualBackgroundViewModel
            every { virtualBackgroundViewModel.uiState } returns MutableStateFlow(
                VirtualBackgroundUiState(backgroundList = mockVirtualBackgrounds)
            )
        }

        @AfterClass
        @JvmStatic
        fun destroy() {
            unmockkAll()
        }
    }
}