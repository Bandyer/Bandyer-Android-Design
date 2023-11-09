package com.kaleyra.video_sdk.viewmodel.call

import android.content.Context
import androidx.fragment.app.FragmentActivity
import com.bandyer.android_audiosession.model.AudioOutputDevice
import com.kaleyra.video.Company
import com.kaleyra.video.Contact
import com.kaleyra.video.conference.*
import com.kaleyra.video.conference.Call
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.ConversationUI
import com.kaleyra.video_common_ui.CollaborationViewModel.Configuration
import com.kaleyra.video_common_ui.ConferenceUI
import com.kaleyra.video_common_ui.call.CameraStreamPublisher.Companion.CAMERA_STREAM_ID
import com.kaleyra.video_extension_audio.extensions.CollaborationAudioExtensions
import com.kaleyra.video_extension_audio.extensions.CollaborationAudioExtensions.currentAudioOutputDevice
import com.kaleyra.video_sdk.MainDispatcherRule
import com.kaleyra.video_sdk.call.audiooutput.model.AudioDeviceUi
import com.kaleyra.video_sdk.call.callactions.model.CallAction
import com.kaleyra.video_sdk.call.callactions.viewmodel.CallActionsViewModel
import com.kaleyra.video_sdk.call.screenshare.viewmodel.ScreenShareViewModel.Companion.SCREEN_SHARE_STREAM_ID
import com.kaleyra.video_sdk.common.usermessages.model.CameraRestrictionMessage
import com.kaleyra.video_sdk.common.usermessages.provider.CallUserMessagesProvider
import io.mockk.*
import io.mockk.Ordering.ORDERED
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
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

    private val conferenceMock = mockk<ConferenceUI>()

    private val conversationMock = mockk<ConversationUI>(relaxed = true)

    private val companyMock = mockk<Company>(relaxed = true)

    private val callMock = mockk<CallUI>(relaxed = true)

    private val callParticipantsMock = mockk<CallParticipants>()

    private val meMock = mockk<CallParticipant.Me>(relaxed = true)

    private val otherParticipantMock = mockk<CallParticipant>()

    private val otherParticipantMock2 = mockk<CallParticipant>()

    private val inputsMock = mockk<Inputs>()

    private val audioMock = mockk<Input.Audio>(relaxed = true)

    private val myStreamMock = mockk<Stream.Mutable>()

    private val videoMock = mockk<Input.Video.Camera.Internal>(relaxed = true)

    private val externalCameraMock = mockk<Input.Video.Camera.Usb>(relaxed = true)

    private val rearLens = mockk<Input.Video.Camera.Internal.Lens>()

    private val frontLens = mockk<Input.Video.Camera.Internal.Lens>()

    private val effectsMock = mockk<Effects>()

    private val restrictionMock = mockk<Contact.Restrictions>()

    private val cameraRestrictionMock = mockk<Contact.Restrictions.Restriction.Camera>()

    private val inputs = MutableStateFlow(setOf<Input>())

    private val callState = MutableStateFlow<Call.State>(Call.State.Disconnected)

    private val activity = mockk<FragmentActivity>()

    @Before
    fun setUp() {
        viewModel = spyk(CallActionsViewModel {
            Configuration.Success(conferenceMock, conversationMock, companyMock, MutableStateFlow(mockk()))
        })
        every { conferenceMock.call } returns MutableStateFlow(callMock)
        every { companyMock.id } returns MutableStateFlow("companyId")
        with(callMock) {
            every { serverId } returns MutableStateFlow("serverId")
            every { inputs } returns inputsMock
            every { actions } returns MutableStateFlow(setOf(CallUI.Action.HangUp, CallUI.Action.Audio))
            every { state } returns callState
            every { participants } returns MutableStateFlow(callParticipantsMock)
            every { effects } returns effectsMock
            every { preferredType } returns MutableStateFlow(Call.PreferredType.audioVideo())
        }
        with(callParticipantsMock) {
            every { me } returns meMock
            every { others } returns listOf(otherParticipantMock)
        }
        with(myStreamMock) {
            every { id } returns CAMERA_STREAM_ID
            every { video } returns MutableStateFlow(videoMock)
            every { audio } returns MutableStateFlow(audioMock)
        }
        with(effectsMock) {
            every { available } returns MutableStateFlow(setOf())
            every { preselected } returns MutableStateFlow(Effect.Video.None)
        }
        every { otherParticipantMock.userId } returns "otherUserId"
        every { otherParticipantMock2.userId } returns "otherUserId2"

        inputs.value = setOf(audioMock, videoMock)
        every { inputsMock.availableInputs } returns inputs

        coEvery { inputsMock.request(any(), Inputs.Type.Microphone) } coAnswers {
            Inputs.RequestResult.Success(audioMock).apply {
                inputs.value = setOf(input)
            }
        }
        coEvery { inputsMock.request(any(), Inputs.Type.Camera.Internal) } coAnswers {
            Inputs.RequestResult.Success(videoMock).apply {
                inputs.value = setOf(input)
            }
        }
        coEvery { inputsMock.request(any(), Inputs.Type.Camera.External) } coAnswers {
            Inputs.RequestResult.Success(externalCameraMock).apply {
                inputs.value = setOf(input)
            }
        }
        every { videoMock.lenses } returns listOf(frontLens, rearLens)
        with(meMock) {
            every { streams } returns MutableStateFlow(listOf(myStreamMock))
            every { restrictions } returns restrictionMock
        }
        every { restrictionMock.camera } returns MutableStateFlow(cameraRestrictionMock)
        every { cameraRestrictionMock.usage } returns false
        every { videoMock.enabled } returns MutableStateFlow(false)
        every { videoMock.state } returns MutableStateFlow(Input.State.Active)
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
        val expected = listOf(CallAction.Audio(), CallAction.HangUp())
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
        val expected = listOf(CallAction.VirtualBackground(isToggled = true), CallAction.HangUp())
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
    fun testCallActionsUiState_callStateEnded_actionsNotUpdated() = runTest {
        val result = viewModel.uiState
        val current = result.first().actionList.value
        assertEquals(listOf<CallAction>(), current)

        callState.value = Call.State.Disconnected.Ended

        advanceUntilIdle()
        val actual = result.first().actionList.value
        assertEquals(listOf<CallAction>(), actual)
    }

    @Test
    fun testCallActionsUiState_callStateDisconnecting_actionsNotUpdated() = runTest {
        val result = viewModel.uiState
        val current = result.first().actionList.value
        assertEquals(listOf<CallAction>(), current)

        callState.value = Call.State.Disconnecting

        advanceUntilIdle()
        val actual = result.first().actionList.value
        assertEquals(listOf<CallAction>(), actual)
    }

    @Test
    fun callIsNotConnected_callActionsUiState_fileShareIsDisabled() = runTest {
        val result = viewModel.uiState
        val current = result.first().actionList.value
        assertEquals(listOf<CallAction>(), current)

        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected)
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

        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected)
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

        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected)
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
    fun meParticipantIsNull_callActionsUiState_microphoneIsDisabled() = runTest {
        val result = viewModel.uiState
        val current = result.first().actionList.value
        assertEquals(listOf<CallAction>(), current)

        every { callParticipantsMock.me } returns null
        every { callMock.actions } returns MutableStateFlow(setOf(CallUI.Action.ToggleMicrophone))

        advanceUntilIdle()
        val actual = result.first().actionList.value.first().isEnabled
        assertEquals(false, actual)
    }

    @Test
    fun meParticipantIsNull_callActionsUiState_cameraIsDisabled() = runTest {
        val result = viewModel.uiState
        val current = result.first().actionList.value
        assertEquals(listOf<CallAction>(), current)

        every { callParticipantsMock.me } returns null
        every { callMock.actions } returns MutableStateFlow(setOf(CallUI.Action.ToggleCamera))

        advanceUntilIdle()
        val actual = result.first().actionList.value.first().isEnabled
        assertEquals(false, actual)
    }

    @Test
    fun meParticipantIsNotNull_callActionsUiState_microphoneIsEnabled() = runTest {
        val result = viewModel.uiState
        val current = result.first().actionList.value
        assertEquals(listOf<CallAction>(), current)

        every { callParticipantsMock.me } returns meMock
        every { callMock.actions } returns MutableStateFlow(setOf(CallUI.Action.ToggleMicrophone))

        advanceUntilIdle()
        val actual = result.first().actionList.value.first().isEnabled
        assertEquals(true, actual)
    }

    @Test
    fun meParticipantIsNotNull_callActionsUiState_cameraIsEnabled() = runTest {
        val result = viewModel.uiState
        val current = result.first().actionList.value
        assertEquals(listOf<CallAction>(), current)

        every { callParticipantsMock.me } returns meMock
        every { callMock.actions } returns MutableStateFlow(setOf(CallUI.Action.ToggleCamera))

        advanceUntilIdle()
        val actual = result.first().actionList.value.first().isEnabled
        assertEquals(true, actual)
    }

    @Test
    fun usbCameraConnected_callActionsUiState_switchCameraIsDisabled() = runTest {
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf(mockk<Input.Video.Camera.Usb>()))
        every { callMock.actions } returns MutableStateFlow(setOf(CallUI.Action.SwitchCamera))

        advanceUntilIdle()
        val result = viewModel.uiState
        val actual = result.first().actionList.value
        val expected = listOf(CallAction.SwitchCamera(isEnabled = false))
        assertEquals(expected, actual)
    }

    @Test
    fun usbCameraNotConnectedAndCameraEnable_callActionsUiState_switchCameraIsEnabled() = runTest {
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf(mockk<Input.Video.Camera.Internal>()))
        every { callMock.actions } returns MutableStateFlow(setOf(CallUI.Action.SwitchCamera))
        every { videoMock.enabled } returns MutableStateFlow(true)

        advanceUntilIdle()
        val result = viewModel.uiState
        val actual = result.first().actionList.value
        val expected = listOf(CallAction.SwitchCamera(isEnabled = true))
        assertEquals(expected, actual)
    }


    @Test
    fun usbCameraNotConnectedAndCameraDisable_callActionsUiState_switchCameraIsDisabled() = runTest {
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf(mockk<Input.Video.Camera.Internal>()))
        every { callMock.actions } returns MutableStateFlow(setOf(CallUI.Action.SwitchCamera))
        every { videoMock.enabled } returns MutableStateFlow(false)

        advanceUntilIdle()
        val result = viewModel.uiState
        val actual = result.first().actionList.value
        val expected = listOf(CallAction.SwitchCamera(isEnabled = false))
        assertEquals(expected, actual)
    }

    @Test
    fun testToggleMicOn() = runTest {
        every { audioMock.enabled } returns MutableStateFlow(false)
        runCurrent()
        viewModel.toggleMic(activity)
        runCurrent()
        verify(exactly = 1) { audioMock.tryEnable() }
    }

    @Test
    fun testToggleMicOnWithoutAvailableInput() = runTest {
        inputs.value = setOf()
        every { audioMock.enabled } returns MutableStateFlow(false)
        advanceUntilIdle()
        viewModel.toggleMic(activity)
        advanceUntilIdle()
        coVerify(exactly = 1) { inputsMock.request(any(), Inputs.Type.Microphone) }
        verify(exactly = 1) { audioMock.tryEnable() }
    }

    @Test
    fun testToggleMicOff() = runTest {
        every { audioMock.enabled } returns MutableStateFlow(true)
        runCurrent()
        viewModel.toggleMic(activity)
        runCurrent()
        verify(exactly = 1) { audioMock.tryDisable() }
    }

    @Test
    fun testToggleMicOffWithoutAvailableInput() = runTest {
        inputs.value = setOf()
        every { audioMock.enabled } returns MutableStateFlow(true)
        advanceUntilIdle()
        viewModel.toggleMic(activity)
        advanceUntilIdle()
        coVerify(exactly = 1) { inputsMock.request(any(), Inputs.Type.Microphone) }
        verify(exactly = 1) { audioMock.tryDisable() }
    }

    @Test
    fun testToggleCameraOn() = runTest {
        every { videoMock.enabled } returns MutableStateFlow(false)
        runCurrent()
        viewModel.toggleCamera(activity)
        runCurrent()
        verify(exactly = 1) { videoMock.tryEnable() }
    }

    @Test
    fun testToggleCameraOnWithoutAvailableInput() = runTest {
        inputs.value = setOf()
        every { videoMock.enabled } returns MutableStateFlow(false)
        advanceUntilIdle()
        viewModel.toggleCamera(activity)
        advanceUntilIdle()
        coVerify(ordering = ORDERED) {
            inputsMock.request(any(), Inputs.Type.Camera.External)
            inputsMock.request(any(), Inputs.Type.Camera.Internal)
        }
        verify(exactly = 1) { videoMock.tryEnable() }
    }

    @Test
    fun testToggleCameraOff() = runTest {
        every { videoMock.enabled } returns MutableStateFlow(true)
        runCurrent()
        viewModel.toggleCamera(activity)
        runCurrent()
        verify(exactly = 1) { videoMock.tryDisable() }
    }

    @Test
    fun testToggleCameraOffWithoutAvailableInput() = runTest {
        inputs.value = setOf()
        every { videoMock.enabled } returns MutableStateFlow(true)
        advanceUntilIdle()
        viewModel.toggleCamera(activity)
        advanceUntilIdle()
        coVerify(ordering = ORDERED) {
            inputsMock.request(any(), Inputs.Type.Camera.External)
            inputsMock.request(any(), Inputs.Type.Camera.Internal)
        }
        verify(exactly = 1) { videoMock.tryDisable() }
    }

    @Test
    fun testToggleCameraWithCameraRestriction() = runTest {
        mockkObject(CallUserMessagesProvider)
        every { cameraRestrictionMock.usage } returns true
        runCurrent()
        viewModel.toggleCamera(activity)
        runCurrent()
        verify(exactly = 1) {
            CallUserMessagesProvider.sendUserMessage(withArg {
                assertEquals(it::class, CameraRestrictionMessage::class)
            })
        }
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
        every { conversationMock.chat(any(), any()) } returns Result.success(mockk())
        advanceUntilIdle()
        viewModel.showChat(contextMock)
        val expectedUserId = otherParticipantMock.userId
        verify(exactly = 1) {
            conversationMock.chat(
                context = contextMock,
                userId = withArg { assertEquals(it, expectedUserId) }
            )
        }
    }

    @Test
    fun testShowGroupChat() = runTest {
        val contextMock = mockk<Context>()
        every { conversationMock.chat(any(), any(), any()) } returns Result.success(mockk())
        every { callParticipantsMock.others } returns listOf(otherParticipantMock, otherParticipantMock2)
        advanceUntilIdle()
        viewModel.showChat(contextMock)
        advanceUntilIdle()
        val expectedCallServerId = callMock.serverId.replayCache.first()
        val expectedUserIds = listOf(otherParticipantMock.userId, otherParticipantMock2.userId)
        verify(exactly = 1) {
            conversationMock.chat(
                context = contextMock,
                userIds = withArg { assertEquals(it, expectedUserIds) },
                chatId = withArg { assertEquals(it, expectedCallServerId) }
            )
        }
    }

    @Test
    fun testShowChatWithCompanyIdParticipant() = runTest {
        val contextMock = mockk<Context>()
        val companyParticipant = mockk<CallParticipant> {
            every { userId } returns "companyId"
        }
        every { conversationMock.chat(any(), any()) } returns Result.success(mockk())
        every { callParticipantsMock.others } returns listOf(otherParticipantMock, companyParticipant)
        advanceUntilIdle()
        viewModel.showChat(contextMock)
        val expectedUserId = otherParticipantMock.userId
        verify(exactly = 1) {
            conversationMock.chat(
                context = contextMock,
                userId = withArg { assertEquals(it, expectedUserId) }
            )
        }
    }

    @Test
    fun testShowGroupChatWithCompanyIdParticipant() = runTest {
        val contextMock = mockk<Context>()
        val companyParticipant = mockk<CallParticipant> {
            every { userId } returns "companyId"
        }
        every { conversationMock.chat(any(), any(), any()) } returns Result.success(mockk())
        every { callParticipantsMock.others } returns listOf(otherParticipantMock, otherParticipantMock2, companyParticipant)
        advanceUntilIdle()
        viewModel.showChat(contextMock)
        advanceUntilIdle()
        val expectedCallServerId = callMock.serverId.replayCache.first()
        val expectedUserIds = listOf(otherParticipantMock.userId, otherParticipantMock2.userId)
        verify(exactly = 1) {
            conversationMock.chat(
                context = contextMock,
                userIds = withArg { assertEquals(it, expectedUserIds) },
                chatId = expectedCallServerId
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
        assertEquals(true, isStopped)
    }
}