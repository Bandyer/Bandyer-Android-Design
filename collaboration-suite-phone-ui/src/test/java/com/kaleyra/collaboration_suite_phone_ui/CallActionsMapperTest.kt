package com.kaleyra.collaboration_suite_phone_ui

import com.bandyer.android_audiosession.model.AudioOutputDevice
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite.phonebox.CallParticipant
import com.kaleyra.collaboration_suite.phonebox.Input
import com.kaleyra.collaboration_suite.phonebox.Stream
import com.kaleyra.collaboration_suite_core_ui.CallUI
import com.kaleyra.collaboration_suite_extension_audio.extensions.CollaborationAudioExtensions
import com.kaleyra.collaboration_suite_extension_audio.extensions.CollaborationAudioExtensions.currentAudioOutputDevice
import com.kaleyra.collaboration_suite_phone_ui.Mocks.callMock
import com.kaleyra.collaboration_suite_phone_ui.call.compose.audiooutput.model.AudioDeviceUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.model.CallAction
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.model.CallActionsMapper.isConnected
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.model.CallActionsMapper.isMyCameraEnabled
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.model.CallActionsMapper.isMyMicEnabled
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.model.CallActionsMapper.toCallActions
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.model.CallActionsMapper.toCurrentAudioDeviceUi
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.*
import org.junit.Assert.assertEquals

class CallActionsMapperTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private val participantMeMock = mockk<CallParticipant.Me>()

    private val videoMock = mockk<Input.Video.Camera.Internal>()

    private val audioMock = mockk<Input.Audio>()

    private val streamMock = mockk<Stream.Mutable> {
        every { this@mockk.video } returns MutableStateFlow(videoMock)
        every { this@mockk.audio } returns MutableStateFlow(audioMock)
    }

    @Before
    fun setUp() {
        every { callMock.participants } returns MutableStateFlow(mockk {
            every { me } returns participantMeMock
        })
        every { participantMeMock.streams } returns MutableStateFlow(listOf(streamMock))
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun callConnected_isConnected_true() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Connected)
        val call = MutableStateFlow(callMock)
        val actual = call.isConnected().first()
        assertEquals(true, actual)
    }


    @Test
    fun callUnknownError_isConnected_false() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended.Error.Unknown())
        val call = MutableStateFlow(callMock)
        val actual = call.isConnected().first()
        assertEquals(false, actual)
    }

    @Test
    fun callServerError_isConnected_false() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended.Error.Server())
        val call = MutableStateFlow(callMock)
        val actual = call.isConnected().first()
        assertEquals(false, actual)
    }

    @Test
    fun callError_isConnected_false() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended.Error)
        val call = MutableStateFlow(callMock)
        val actual = call.isConnected().first()
        assertEquals(false, actual)
    }

    @Test
    fun callTimeout_isConnected_false() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended.Timeout)
        val call = MutableStateFlow(callMock)
        val actual = call.isConnected().first()
        assertEquals(false, actual)
    }

    @Test
    fun callLineBusy_isConnected_false() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended.LineBusy)
        val call = MutableStateFlow(callMock)
        val actual = call.isConnected().first()
        assertEquals(false, actual)
    }

    @Test
    fun callAnsweredOnAnotherDevice_isConnected_false() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended.AnsweredOnAnotherDevice)
        val call = MutableStateFlow(callMock)
        val actual = call.isConnected().first()
        assertEquals(false, actual)
    }

    @Test
    fun callKicked_isConnected_false() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended.Kicked(""))
        val call = MutableStateFlow(callMock)
        val actual = call.isConnected().first()
        assertEquals(false, actual)
    }

    @Test
    fun callDeclined_isConnected_false() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended.Declined)
        val call = MutableStateFlow(callMock)
        val actual = call.isConnected().first()
        assertEquals(false, actual)
    }

    @Test
    fun callHungUp_isConnected_false() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended.HungUp())
        val call = MutableStateFlow(callMock)
        val actual = call.isConnected().first()
        assertEquals(false, actual)
    }

    @Test
    fun callEnded_isConnected_false() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended)
        val call = MutableStateFlow(callMock)
        val actual = call.isConnected().first()
        assertEquals(false, actual)
    }

    @Test
    fun callDisconnected_isConnected_false() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected)
        val call = MutableStateFlow(callMock)
        val actual = call.isConnected().first()
        assertEquals(false, actual)
    }

    @Test
    fun callConnecting_isConnected_false() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Connecting)
        val call = MutableStateFlow(callMock)
        val actual = call.isConnected().first()
        assertEquals(false, actual)
    }

    @Test
    fun callReconnecting_isConnected_false() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Reconnecting)
        val call = MutableStateFlow(callMock)
        val actual = call.isConnected().first()
        assertEquals(false, actual)
    }

    @Test
    fun loudSpeakerAudioOutputDevice_toCurrentAudioDeviceUi_loudspeakerAudioDeviceUi() = runTest {
        mockkObject(CollaborationAudioExtensions)
        every { any<Call>().currentAudioOutputDevice } returns MutableStateFlow(AudioOutputDevice.LOUDSPEAKER())
        val call = MutableStateFlow(callMock)
        val result = call.toCurrentAudioDeviceUi()
        val actual = result.first()
        val expected = AudioDeviceUi.LoudSpeaker
        assertEquals(expected, actual)
    }

    @Test
    fun earPieceAudioOutputDevice_toCurrentAudioDeviceUi_earPieceAudioDeviceUi() = runTest {
        mockkObject(CollaborationAudioExtensions)
        every { any<Call>().currentAudioOutputDevice } returns MutableStateFlow(AudioOutputDevice.EARPIECE())
        val call = MutableStateFlow(callMock)
        val result = call.toCurrentAudioDeviceUi()
        val actual = result.first()
        val expected = AudioDeviceUi.EarPiece
        assertEquals(expected, actual)
    }

    @Test
    fun wiredHeadsetAudioOutputDevice_toCurrentAudioDeviceUi_wiredHeadsetAudioDeviceUi() = runTest {
        mockkObject(CollaborationAudioExtensions)
        every { any<Call>().currentAudioOutputDevice } returns MutableStateFlow(AudioOutputDevice.WIRED_HEADSET())
        val call = MutableStateFlow(callMock)
        val result = call.toCurrentAudioDeviceUi()
        val actual = result.first()
        val expected = AudioDeviceUi.WiredHeadset
        assertEquals(expected, actual)
    }

    @Test
    fun mutedAudioOutputDevice_toCurrentAudioDeviceUi_mutedAudioDeviceUi() = runTest {
        mockkObject(CollaborationAudioExtensions)
        every { any<Call>().currentAudioOutputDevice } returns MutableStateFlow(AudioOutputDevice.NONE())
        val call = MutableStateFlow(callMock)
        val result = call.toCurrentAudioDeviceUi()
        val actual = result.first()
        val expected = AudioDeviceUi.Muted
        assertEquals(expected, actual)
    }

    @Test
    fun bluetoothAudioOutputDevice_toCurrentAudioDeviceUi_bluetoothAudioDeviceUi() = runTest {
        mockkObject(CollaborationAudioExtensions)
        every { any<Call>().currentAudioOutputDevice } returns MutableStateFlow(AudioOutputDevice.BLUETOOTH())
        val call = MutableStateFlow(callMock)
        val result = call.toCurrentAudioDeviceUi()
        val actual = result.first()?.javaClass
        val expected = AudioDeviceUi.Bluetooth::class.java
        assertEquals(expected, actual)
    }

    @Test
    fun emptyCallActions_toCallActions_emptyList() = runTest {
        every { callMock.state } returns MutableStateFlow(mockk())
        every { callMock.actions } returns MutableStateFlow(emptySet())
        val call = MutableStateFlow(callMock)
        val result = call.toCallActions()
        val actual = result.first()
        val expected = listOf<CallAction>()
        assertEquals(expected, actual)
    }

    @Test
    fun filledCallActions_toCallActions_mappedCallActions() = runTest {
        every { callMock.state } returns MutableStateFlow(mockk())
        every { callMock.actions } returns MutableStateFlow(
            setOf(
                CallUI.Action.ToggleMicrophone,
                CallUI.Action.ToggleCamera,
                CallUI.Action.SwitchCamera,
                CallUI.Action.HangUp,
                CallUI.Action.OpenChat.Full,
                CallUI.Action.OpenWhiteboard.Full,
                CallUI.Action.Audio,
                CallUI.Action.FileShare,
                CallUI.Action.ScreenShare
            )
        )
        val call = MutableStateFlow(callMock)
        val result = call.toCallActions()
        val actual = result.first()
        val expected = listOf(
            CallAction.Microphone(),
            CallAction.Camera(),
            CallAction.SwitchCamera(),
            CallAction.HangUp(),
            CallAction.Chat(),
            CallAction.Whiteboard(),
            CallAction.Audio(),
            CallAction.FileShare(),
            CallAction.ScreenShare(),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun videoEnabled_isMyCameraEnabled_true() = runTest {
        every { videoMock.enabled } returns MutableStateFlow(true)
        val call = MutableStateFlow(callMock)
        val result = call.isMyCameraEnabled()
        val actual = result.first()
        assertEquals(true, actual)
    }

    @Test
    fun videoDisabled_isMyCameraEnabled_false() = runTest {
        every { videoMock.enabled } returns MutableStateFlow(false)
        val call = MutableStateFlow(callMock)
        val result = call.isMyCameraEnabled()
        val actual = result.first()
        assertEquals(false, actual)
    }

    @Test
    fun audioEnabled_isMyMicEnabled_true() = runTest {
        every { audioMock.enabled } returns MutableStateFlow(true)
        val call = MutableStateFlow(callMock)
        val result = call.isMyMicEnabled()
        val actual = result.first()
        assertEquals(true, actual)
    }

    @Test
    fun audioDisable_isMyMicEnabled_false() = runTest {
        every { audioMock.enabled } returns MutableStateFlow(false)
        val call = MutableStateFlow(callMock)
        val result = call.isMyMicEnabled()
        val actual = result.first()
        assertEquals(false, actual)
    }
}