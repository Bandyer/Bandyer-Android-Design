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