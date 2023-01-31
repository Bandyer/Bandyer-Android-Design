package com.kaleyra.collaboration_suite_phone_ui

import android.content.Context
import com.bandyer.android_audiosession.model.AudioOutputDevice
import com.kaleyra.collaboration_suite.phonebox.*
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite_core_ui.CallUI
import com.kaleyra.collaboration_suite_core_ui.Configuration
import com.kaleyra.collaboration_suite_extension_audio.extensions.CollaborationAudioExtensions
import com.kaleyra.collaboration_suite_extension_audio.extensions.CollaborationAudioExtensions.currentAudioOutputDevice
import com.kaleyra.collaboration_suite_phone_ui.Mocks.callMock
import com.kaleyra.collaboration_suite_phone_ui.Mocks.chatBoxMock
import com.kaleyra.collaboration_suite_phone_ui.Mocks.chatMock
import com.kaleyra.collaboration_suite_phone_ui.call.compose.audiooutput.model.AudioDeviceUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.model.CallAction
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.model.CallActionsMapper.toCallActions
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.viewmodel.CallActionsViewModel
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CallActionsViewModelTest {

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: CallActionsViewModel

    private val callParticipantsMock = mockk<CallParticipants>()

    private val meMock = mockk<CallParticipant.Me>()

    private val otherParticipantMock = mockk<CallParticipant> {
        every { userId } returns "otherUserId"
    }

    private val inputsMock = mockk<Inputs>()

    private val audioMock = mockk<Input.Audio>(relaxed = true)

    private val myStreamMock = mockk<Stream.Mutable>()

    private val videoMock = mockk<Input.Video.Camera.Internal>(relaxed = true)

    private val rearLens = mockk<Input.Video.Camera.Internal.Lens> {
        every { isRear } returns false
    }

    private val frontLens = mockk<Input.Video.Camera.Internal.Lens> {
        every { isRear } returns true
    }

    @Before
    fun setUp() {
        viewModel = spyk(CallActionsViewModel { Configuration.Success(Mocks.phoneBoxMock, Mocks.chatBoxMock, Mocks.usersDescriptionMock) })
        every { Mocks.phoneBoxMock.call } returns MutableStateFlow(callMock)
        every { callMock.inputs } returns inputsMock
        every { callMock.actions } returns MutableStateFlow(setOf(CallUI.Action.HangUp, CallUI.Action.Audio))
        every { callMock.state } returns MutableStateFlow(mockk())
        every { callMock.participants } returns MutableStateFlow(callParticipantsMock)
        every { callParticipantsMock.me } returns meMock
        every { callParticipantsMock.others } returns listOf(otherParticipantMock)
        every { inputsMock.availableInputs } returns MutableStateFlow(setOf(audioMock, videoMock))
        every { videoMock.lenses } returns listOf(frontLens, rearLens)
        every { meMock.streams } returns MutableStateFlow(listOf(myStreamMock))
        every { myStreamMock.video } returns MutableStateFlow(videoMock)
        every { myStreamMock.audio } returns MutableStateFlow(audioMock)
        every { videoMock.enabled } returns MutableStateFlow(false)
        every { audioMock.enabled } returns MutableStateFlow(false)
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun testCallActionsUiState_actionsUpdated() = runTest {
        advanceUntilIdle()
        val actual = viewModel.uiState.first().actionList.value
        val expected = listOf(CallAction.HangUp(), CallAction.Audio())
        assertEquals(expected, actual)
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
        every { any<Call>().currentAudioOutputDevice } returns MutableStateFlow(AudioOutputDevice.LOUDSPEAKER())
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
    fun testCallActionsUiState_cameraActionUpdated() = runTest {
        every { callMock.actions } returns MutableStateFlow(setOf(CallUI.Action.ToggleCamera))
        every { videoMock.enabled } returns MutableStateFlow(false)

        advanceUntilIdle()
        val result = viewModel.uiState
        val actual = result.first().actionList.value
        val expected = listOf(CallAction.Camera(isToggled = true))
        assertEquals(expected, actual)
    }

    @Test
    fun testCallActionsUiState_micActionUpdated() = runTest {
        every { callMock.actions } returns MutableStateFlow(setOf(CallUI.Action.ToggleMicrophone))
        every { videoMock.enabled } returns MutableStateFlow(false)

        advanceUntilIdle()
        val result = viewModel.uiState
        val actual = result.first().actionList.value
        val expected = listOf(CallAction.Microphone(isToggled = true))
        assertEquals(expected, actual)
    }


    @Test
    fun testCallActionsUiState_audioActionUpdated() = runTest {
        mockkObject(CollaborationAudioExtensions)
        every { callMock.actions } returns MutableStateFlow(setOf(CallUI.Action.Audio))
        every { any<Call>().currentAudioOutputDevice } returns MutableStateFlow(AudioOutputDevice.LOUDSPEAKER())

        advanceUntilIdle()
        val result = viewModel.uiState
        val actual = result.first().actionList.value
        val expected = listOf(CallAction.Audio(device = AudioDeviceUi.LoudSpeaker))
        assertEquals(expected, actual)
    }

    @Test
    fun testToggleMicOn() = runTest {
        every { audioMock.enabled } returns MutableStateFlow(false)
        advanceUntilIdle()
        viewModel.toggleMic()
        verify { audioMock.tryEnable() }
    }

    @Test
    fun testToggleMicOff() = runTest {
        every { audioMock.enabled } returns MutableStateFlow(true)
        advanceUntilIdle()
        viewModel.toggleMic()
        verify { audioMock.tryDisable() }
    }

    @Test
    fun testToggleCameraOn() = runTest {
        every { videoMock.enabled } returns MutableStateFlow(false)
        advanceUntilIdle()
        viewModel.toggleCamera()
        verify { videoMock.tryEnable() }
    }

    @Test
    fun testToggleCameraOff() = runTest {
        every { videoMock.enabled } returns MutableStateFlow(true)
        advanceUntilIdle()
        viewModel.toggleCamera()
        verify { videoMock.tryDisable() }
    }

    @Test
    fun testHangUp() = runTest {
        advanceUntilIdle()
        viewModel.hangUp()
        verify { callMock.end() }
    }

    @Test
    fun testSwitchCameraToFrontLens() = runTest {
        advanceUntilIdle()
        every { videoMock.currentLens } returns MutableStateFlow(rearLens)
        viewModel.switchCamera()
        verify { videoMock.setLens(frontLens) }
    }

    @Test
    fun testSwitchCameraToRearLens() = runTest {
        advanceUntilIdle()
        every { videoMock.currentLens } returns MutableStateFlow(frontLens)
        viewModel.switchCamera()
        verify { videoMock.setLens(rearLens) }
    }

    @Test
    fun testShowChat() = runTest {
        val contextMock = mockk<Context>()
        every { chatBoxMock.chat(any(), any()) } returns Result.success(mockk())
        advanceUntilIdle()
        viewModel.showChat(contextMock)
        val expectedUserIds = listOf(otherParticipantMock.userId)
        verify {
            chatBoxMock.chat(
                context = contextMock,
                userIDs = withArg { assertEquals(it, expectedUserIds) }
            )
        }
    }

    @Test
    fun testStopDeviceScreenShare() = runTest {

    }

    @Test
    fun testStopAppScreenShare() {
        testStopScreenShare(mockk<Input.Video.Application>())
    }

    private fun testStopScreenShare(videoScreenMock: Input.Video) = runTest {
        every { videoScreenMock.enabled } returns MutableStateFlow(true)
        every { videoScreenMock.tryDisable() } returns true
        every { inputsMock.availableInputs } returns MutableStateFlow(setOf(videoScreenMock))
        advanceUntilIdle()
        val isStopped = viewModel.stopScreenShare()
        verify { videoScreenMock.tryDisable() }
        assertEquals(true , isStopped)
    }
}