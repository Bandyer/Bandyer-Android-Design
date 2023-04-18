package com.kaleyra.collaboration_suite_phone_ui

import android.content.Context
import com.bandyer.android_audiosession.model.AudioOutputDevice
import com.kaleyra.collaboration_suite.phonebox.*
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite_core_ui.CallUI
import com.kaleyra.collaboration_suite_core_ui.ChatBoxUI
import com.kaleyra.collaboration_suite_core_ui.Configuration
import com.kaleyra.collaboration_suite_core_ui.PhoneBoxUI
import com.kaleyra.collaboration_suite_core_ui.call.CameraStreamPublisher.Companion.CAMERA_STREAM_ID
import com.kaleyra.collaboration_suite_extension_audio.extensions.CollaborationAudioExtensions
import com.kaleyra.collaboration_suite_extension_audio.extensions.CollaborationAudioExtensions.currentAudioOutputDevice
import com.kaleyra.collaboration_suite_phone_ui.call.compose.audiooutput.model.AudioDeviceUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.model.CallAction
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.viewmodel.CallActionsViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.compose.screenshare.viewmodel.ScreenShareViewModel.Companion.SCREEN_SHARE_STREAM_ID
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CallActionsViewModelTest {

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: CallActionsViewModel

    private val phoneBoxMock = mockk<PhoneBoxUI>()

    private val chatBoxMock = mockk<ChatBoxUI>(relaxed = true)

    private val callMock = mockk<CallUI>(relaxed = true)

    private val callParticipantsMock = mockk<CallParticipants>()

    private val meMock = mockk<CallParticipant.Me>(relaxed = true)

    private val otherParticipantMock = mockk<CallParticipant>()

    private val inputsMock = mockk<Inputs>()

    private val audioMock = mockk<Input.Audio>(relaxed = true)

    private val myStreamMock = mockk<Stream.Mutable>()

    private val videoMock = mockk<Input.Video.Camera.Internal>(relaxed = true)

    private val rearLens = mockk<Input.Video.Camera.Internal.Lens>()

    private val frontLens = mockk<Input.Video.Camera.Internal.Lens>()

    private val effectsMock = mockk<Effects>()

    @Before
    fun setUp() {
        viewModel = spyk(CallActionsViewModel { Configuration.Success(phoneBoxMock, chatBoxMock, mockk()) })
        every { phoneBoxMock.call } returns MutableStateFlow(callMock)
        with(callMock) {
            every { inputs } returns inputsMock
            every { actions } returns MutableStateFlow(setOf(CallUI.Action.HangUp, CallUI.Action.Audio))
            every { state } returns MutableStateFlow(mockk())
            every { participants } returns MutableStateFlow(callParticipantsMock)
            every { effects }returns effectsMock
        }
        with(callParticipantsMock) {
            every { me } returns meMock
            every { others } returns listOf(otherParticipantMock)
        }
        with(myStreamMock) {
            every { id } returns "myStreamId"
            every { video } returns MutableStateFlow(videoMock)
            every { audio } returns MutableStateFlow(audioMock)
        }
        with(effectsMock) {
            every { available } returns MutableStateFlow(setOf())
            every { preselected } returns MutableStateFlow(Effect.Video.None)
        }
        every { otherParticipantMock.userId }returns "otherUserId"
        every { inputsMock.availableInputs } returns MutableStateFlow(setOf(audioMock, videoMock))
        every { videoMock.lenses } returns listOf(frontLens, rearLens)
        every { meMock.streams } returns MutableStateFlow(listOf(myStreamMock))
        every { videoMock.enabled } returns MutableStateFlow(false)
        every { audioMock.enabled } returns MutableStateFlow(false)
        every { rearLens.isRear } returns false
        every { frontLens.isRear } returns true
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun testCallActionsUiState_actionsUpdated() = runTest {
        val current = viewModel.uiState.first().actionList.value
        assertEquals(listOf<CallAction>(), current)
        advanceUntilIdle()
        val new = viewModel.uiState.first().actionList.value
        val expected = listOf(CallAction.HangUp(), CallAction.Audio())
        assertEquals(expected, new)
    }

    @Test
    fun testCallActionsUiState_cameraActionKeepsStateAfterCallActionsUpdate() = runTest {
        val actions = MutableStateFlow(setOf(CallUI.Action.ToggleCamera, CallUI.Action.HangUp))
        every { callMock.actions } returns actions
        advanceUntilIdle()
        val result = viewModel.uiState
        val actual = result.first().actionList.value
        val expected = listOf(CallAction.Camera(isToggled = true), CallAction.HangUp())
        assertEquals(expected, actual)

        // Update call actions
        actions.value = setOf(CallUI.Action.ToggleCamera)
        advanceUntilIdle()
        val newActual = result.first().actionList.value
        val newExpected = listOf(CallAction.Camera(isToggled = true))
        assertEquals(newExpected, newActual)
    }

    @Test
    fun testCallActionsUiState_micActionKeepsStateAfterCallActionsUpdate() = runTest {
        val actions = MutableStateFlow(setOf(CallUI.Action.ToggleMicrophone, CallUI.Action.HangUp))
        every { callMock.actions } returns actions
        advanceUntilIdle()
        val result = viewModel.uiState
        val actual = result.first().actionList.value
        val expected = listOf(CallAction.Microphone(isToggled = true), CallAction.HangUp())
        assertEquals(expected, actual)

        // Update call actions
        actions.value = setOf(CallUI.Action.ToggleMicrophone)
        advanceUntilIdle()
        val newActual = result.first().actionList.value
        val newExpected = listOf(CallAction.Microphone(isToggled = true))
        assertEquals(newExpected, newActual)
    }

    @Test
    fun testCallActionsUiState_audioActionKeepsStateAfterCallActionsUpdate() = runTest {
        mockkObject(CollaborationAudioExtensions)
        every { any<Call>().currentAudioOutputDevice } returns MutableStateFlow(AudioOutputDevice.Loudspeaker())
        val actions = MutableStateFlow(setOf(CallUI.Action.Audio, CallUI.Action.HangUp))
        every { callMock.actions } returns actions
        advanceUntilIdle()
        val result = viewModel.uiState
        val actual = result.first().actionList.value
        val expected = listOf(CallAction.Audio(device = AudioDeviceUi.LoudSpeaker), CallAction.HangUp())
        assertEquals(expected, actual)

        // Update call actions
        actions.value = setOf(CallUI.Action.Audio)
        advanceUntilIdle()
        val newActual = result.first().actionList.value
        val newExpected = listOf(CallAction.Audio(device = AudioDeviceUi.LoudSpeaker))
        assertEquals(newExpected, newActual)
    }

    @Test
    fun testCallActionsUiState_virtualBackgroundKeepsStateAfterCallActionsUpdate() = runTest {
        with(effectsMock) {
            every { available } returns MutableStateFlow(setOf(Effect.Video.Background.Blur(factor = 1f)))
            every { preselected } returns MutableStateFlow(Effect.Video.Background.Image(image = mockk()))
        }
        every { myStreamMock.id } returns CAMERA_STREAM_ID
        every { videoMock.currentEffect } returns MutableStateFlow(Effect.Video.Background.Image(image = mockk()))
        val actions = MutableStateFlow(setOf<CallUI.Action>(CallUI.Action.HangUp))
        every { callMock.actions } returns actions
        advanceUntilIdle()
        val result = viewModel.uiState
        val actual = result.first().actionList.value
        val expected = listOf(CallAction.HangUp(), CallAction.VirtualBackground(isToggled = true))
        assertEquals(expected, actual)

        // Update call actions
        actions.value = setOf()
        advanceUntilIdle()
        val newActual = result.first().actionList.value
        val newExpected = listOf(CallAction.VirtualBackground(isToggled = true))
        assertEquals(newExpected, newActual)
    }

    @Test
    fun testCallActionsUiState_cameraActionUpdated() = runTest {
        val result = viewModel.uiState
        val current = result.first().actionList.value
        assertEquals(listOf<CallAction>(), current)

        every { callMock.actions } returns MutableStateFlow(setOf(CallUI.Action.ToggleCamera))
        every { videoMock.enabled } returns MutableStateFlow(false)

        advanceUntilIdle()
        val new = result.first().actionList.value
        val expected = listOf(CallAction.Camera(isToggled = true))
        assertEquals(expected, new)
    }

    @Test
    fun testCallActionsUiState_micActionUpdated() = runTest {
        val result = viewModel.uiState
        val current = result.first().actionList.value
        assertEquals(listOf<CallAction>(), current)

        every { callMock.actions } returns MutableStateFlow(setOf(CallUI.Action.ToggleMicrophone))
        every { audioMock.enabled } returns MutableStateFlow(false)

        advanceUntilIdle()
        val actual = result.first().actionList.value
        val expected = listOf(CallAction.Microphone(isToggled = true))
        assertEquals(expected, actual)
    }


    @Test
    fun testCallActionsUiState_audioActionUpdated() = runTest {
        val result = viewModel.uiState
        val current = result.first().actionList.value
        assertEquals(listOf<CallAction>(), current)

        mockkObject(CollaborationAudioExtensions)
        every { callMock.actions } returns MutableStateFlow(setOf(CallUI.Action.Audio))
        every { any<Call>().currentAudioOutputDevice } returns MutableStateFlow(AudioOutputDevice.Loudspeaker())

        advanceUntilIdle()
        val actual = result.first().actionList.value
        val expected = listOf(CallAction.Audio(device = AudioDeviceUi.LoudSpeaker))
        assertEquals(expected, actual)
    }

    @Test
    fun testCallActionsUiState_virtualBackgroundUpdated() = runTest {
        val result = viewModel.uiState
        val current = result.first().actionList.value
        assertEquals(listOf<CallAction>(), current)

        every { callMock.actions } returns MutableStateFlow(setOf())
        with(effectsMock) {
            every { available } returns MutableStateFlow(setOf(Effect.Video.Background.Blur(factor = 1f)))
            every { preselected } returns MutableStateFlow(Effect.Video.Background.Image(image = mockk()))
        }
        every { myStreamMock.id } returns CAMERA_STREAM_ID
        every { videoMock.currentEffect } returns MutableStateFlow(Effect.Video.Background.Image(image = mockk()))

        advanceUntilIdle()
        val actual = result.first().actionList.value
        val expected = listOf(CallAction.VirtualBackground(isToggled = true))
        assertEquals(expected, actual)
    }

    @Test
    fun callIsNotConnected_callActionsUiState_fileShareIsDisabled() = runTest {
        val result = viewModel.uiState
        val current = result.first().actionList.value
        assertEquals(listOf<CallAction>(), current)

        every { callMock.state } returns MutableStateFlow(mockk())
        every { callMock.actions } returns MutableStateFlow(setOf(CallUI.Action.FileShare))

        advanceUntilIdle()
        val actual = result.first().actionList.value
        val expected = listOf(CallAction.FileShare(isEnabled = false))
        assertEquals(expected, actual)
    }

    @Test
    fun callIsNotConnected_callActionsUiState_screenShareIsDisabled() = runTest {
        val result = viewModel.uiState
        val current = result.first().actionList.value
        assertEquals(listOf<CallAction>(), current)

        every { callMock.state } returns MutableStateFlow(mockk())
        every { callMock.actions } returns MutableStateFlow(setOf(CallUI.Action.ScreenShare))

        advanceUntilIdle()
        val actual = result.first().actionList.value
        val expected = listOf(CallAction.ScreenShare(isEnabled = false))
        assertEquals(expected, actual)
    }

    @Test
    fun callIsNotConnected_callActionsUiState_whiteboardIsDisabled() = runTest {
        val result = viewModel.uiState
        val current = result.first().actionList.value
        assertEquals(listOf<CallAction>(), current)

        every { callMock.state } returns MutableStateFlow(mockk())
        every { callMock.actions } returns MutableStateFlow(setOf(CallUI.Action.OpenWhiteboard.Full))

        advanceUntilIdle()
        val actual = result.first().actionList.value
        val expected = listOf(CallAction.Whiteboard(isEnabled = false))
        assertEquals(expected, actual)
    }

    @Test
    fun callIsConnected_callActionsUiState_fileShareIsEnabled() = runTest {
        val result = viewModel.uiState
        val current = result.first().actionList.value
        assertEquals(listOf<CallAction>(), current)

        every { callMock.state } returns MutableStateFlow(Call.State.Connected)
        every { callMock.actions } returns MutableStateFlow(setOf(CallUI.Action.FileShare))

        advanceUntilIdle()
        val actual = result.first().actionList.value
        val expected = listOf(CallAction.FileShare(isEnabled = true))
        assertEquals(expected, actual)
    }

    @Test
    fun callIsConnected_callActionsUiState_screenShareIsEnabled() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Connected)
        every { callMock.actions } returns MutableStateFlow(setOf(CallUI.Action.ScreenShare))

        advanceUntilIdle()
        val result = viewModel.uiState
        val actual = result.first().actionList.value
        val expected = listOf(CallAction.ScreenShare(isEnabled = true))
        assertEquals(expected, actual)
    }

    @Test
    fun callIsConnected_callActionsUiState_whiteboardIsEnabled() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Connected)
        every { callMock.actions } returns MutableStateFlow(setOf(CallUI.Action.OpenWhiteboard.Full))

        advanceUntilIdle()
        val result = viewModel.uiState
        val actual = result.first().actionList.value
        val expected = listOf(CallAction.Whiteboard(isEnabled = true))
        assertEquals(expected, actual)
    }

    @Test
    fun testToggleMicOn() = runTest {
        every { audioMock.enabled } returns MutableStateFlow(false)
        advanceUntilIdle()
        viewModel.toggleMic()
        verify(exactly = 1) { audioMock.tryEnable() }
    }

    @Test
    fun testToggleMicOff() = runTest {
        every { audioMock.enabled } returns MutableStateFlow(true)
        advanceUntilIdle()
        viewModel.toggleMic()
        verify(exactly = 1) { audioMock.tryDisable() }
    }

    @Test
    fun testToggleCameraOn() = runTest {
        every { videoMock.enabled } returns MutableStateFlow(false)
        advanceUntilIdle()
        viewModel.toggleCamera()
        verify(exactly = 1) { videoMock.tryEnable() }
    }

    @Test
    fun testToggleCameraOff() = runTest {
        every { videoMock.enabled } returns MutableStateFlow(true)
        advanceUntilIdle()
        viewModel.toggleCamera()
        verify(exactly = 1) { videoMock.tryDisable() }
    }

    @Test
    fun testHangUp() = runTest {
        advanceUntilIdle()
        viewModel.hangUp()
        verify(exactly = 1) { callMock.end() }
    }

    @Test
    fun testSwitchCameraToFrontLens() = runTest {
        advanceUntilIdle()
        every { videoMock.currentLens } returns MutableStateFlow(rearLens)
        viewModel.switchCamera()
        verify(exactly = 1) { videoMock.setLens(frontLens) }
    }

    @Test
    fun testSwitchCameraToRearLens() = runTest {
        advanceUntilIdle()
        every { videoMock.currentLens } returns MutableStateFlow(frontLens)
        viewModel.switchCamera()
        verify(exactly = 1) { videoMock.setLens(rearLens) }
    }

    @Test
    fun testShowChat() = runTest {
        val contextMock = mockk<Context>()
        every { chatBoxMock.chat(any(), any()) } returns Result.success(mockk())
        advanceUntilIdle()
        viewModel.showChat(contextMock)
        val expectedUserIds = listOf(otherParticipantMock.userId)
        verify(exactly = 1) {
            chatBoxMock.chat(
                context = contextMock,
                userIDs = withArg { assertEquals(it, expectedUserIds) }
            )
        }
    }

    @Test
    fun testStopDeviceScreenShare() = runTest {
        testTryStopScreenShare(mockk<Input.Video.Screen>())
    }

    @Test
    fun testStopAppScreenShare() {
        testTryStopScreenShare(mockk<Input.Video.Application>())
    }

    private fun testTryStopScreenShare(screenShareVideoMock: Input.Video) = runTest {
        every { screenShareVideoMock.enabled } returns MutableStateFlow(true)
        every { screenShareVideoMock.tryDisable() } returns true
        every { myStreamMock.id } returns SCREEN_SHARE_STREAM_ID
        val availableInputs = setOf(screenShareVideoMock, mockk<Input.Video.Screen>(), mockk<Input.Video.Application>(), mockk<Input.Video.Camera>())
        every { inputsMock.availableInputs } returns MutableStateFlow(availableInputs)
        advanceUntilIdle()
        val isStopped = viewModel.tryStopScreenShare()
        verify(exactly = 1) { screenShareVideoMock.tryDisable() }
        verify(exactly = 1) { meMock.removeStream(myStreamMock) }
        assertEquals(true , isStopped)
    }
}