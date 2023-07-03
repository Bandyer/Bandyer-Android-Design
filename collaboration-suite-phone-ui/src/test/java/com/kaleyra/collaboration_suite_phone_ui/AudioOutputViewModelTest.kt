package com.kaleyra.collaboration_suite_phone_ui

import com.bandyer.android_audiosession.model.AudioOutputDevice
import com.kaleyra.collaboration_suite.phonebox.CallParticipant
import com.kaleyra.collaboration_suite.phonebox.Input
import com.kaleyra.collaboration_suite.phonebox.Stream
import com.kaleyra.collaboration_suite_core_ui.CallUI
import com.kaleyra.collaboration_suite_core_ui.Configuration
import com.kaleyra.collaboration_suite_core_ui.PhoneBoxUI
import com.kaleyra.collaboration_suite_extension_audio.extensions.CollaborationAudioExtensions
import com.kaleyra.collaboration_suite_extension_audio.extensions.CollaborationAudioExtensions.audioOutputDevicesList
import com.kaleyra.collaboration_suite_extension_audio.extensions.CollaborationAudioExtensions.currentAudioOutputDevice
import com.kaleyra.collaboration_suite_extension_audio.extensions.CollaborationAudioExtensions.setAudioOutputDevice
import com.kaleyra.collaboration_suite_phone_ui.call.compose.audiooutput.model.AudioDeviceUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.audiooutput.model.BluetoothDeviceState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.audiooutput.viewmodel.AudioOutputViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.model.CallAction
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AudioOutputViewModelTest {

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: AudioOutputViewModel

    private val phoneBoxMock = mockk<PhoneBoxUI>()

    private val callMock = mockk<CallUI>(relaxed = true)

    private val audioMock1 = mockk<Input.Audio>(relaxed = true)

    private val audioMock2 = mockk<Input.Audio>(relaxed = true)

    private val streamMock1 = mockk<Stream.Mutable>()

    private val streamMock2 = mockk<Stream.Mutable>()

    private val participantMock1 = mockk<CallParticipant>()

    private val participantMock2 = mockk<CallParticipant>()

    @Before
    fun setUp() {
        viewModel = AudioOutputViewModel { Configuration.Success(phoneBoxMock, mockk(), mockk(relaxed = true), mockk(relaxed = true), mockk())}
        mockkObject(CollaborationAudioExtensions)
        every { phoneBoxMock.call } returns MutableStateFlow(callMock)
        every { callMock.audioOutputDevicesList } returns MutableStateFlow(listOf(AudioOutputDevice.Loudspeaker(), AudioOutputDevice.WiredHeadset(), AudioOutputDevice.Earpiece(), AudioOutputDevice.Bluetooth("bluetoothId1"), AudioOutputDevice.Bluetooth("bluetoothId2"), AudioOutputDevice.None()))
        every { callMock.participants } returns MutableStateFlow(mockk {
            every { others } returns listOf(participantMock1, participantMock2)
        })
        every { participantMock1.streams } returns MutableStateFlow(listOf(streamMock1))
        every { participantMock2.streams } returns MutableStateFlow(listOf(streamMock2))
        every { streamMock1.audio } returns MutableStateFlow(audioMock1)
        every { streamMock2.audio } returns MutableStateFlow(audioMock2)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testAudioOutputUiState_playingDeviceIdIsInitialized() = runTest {
        every { callMock.currentAudioOutputDevice } returns MutableStateFlow(AudioOutputDevice.Loudspeaker())
        advanceUntilIdle()
        val new = viewModel.uiState.first().playingDeviceId
        val expected = AudioDeviceUi.LoudSpeaker.id
        Assert.assertEquals(expected, new)
    }

    @Test
    fun testAudioOutputUiState_deviceListUpdated() = runTest {
        every { callMock.audioOutputDevicesList } returns MutableStateFlow(listOf(AudioOutputDevice.Loudspeaker(), AudioOutputDevice.WiredHeadset(), AudioOutputDevice.None()))
        val current = viewModel.uiState.first().audioDeviceList.value
        Assert.assertEquals(listOf<CallAction>(), current)
        advanceUntilIdle()
        val new = viewModel.uiState.first().audioDeviceList.value
        val expected = listOf(AudioDeviceUi.LoudSpeaker, AudioDeviceUi.WiredHeadset, AudioDeviceUi.Muted)
        Assert.assertEquals(expected, new)
    }

    @Test
    fun testSetDevice_playingDeviceIdIsUpdated() = runTest {
        advanceUntilIdle()
        viewModel.setDevice(AudioDeviceUi.LoudSpeaker)
        val actual = viewModel.uiState.first().playingDeviceId
        Assert.assertEquals(AudioDeviceUi.LoudSpeaker.id, actual)
    }

    @Test
    fun testSetLoudSpeakerDevice() = runTest {
        advanceUntilIdle()
        viewModel.setDevice(AudioDeviceUi.LoudSpeaker)
        verify(exactly = 1) { callMock.setAudioOutputDevice(AudioOutputDevice.Loudspeaker()) }
    }

    @Test
    fun testSetEarpieceDevice() = runTest {
        advanceUntilIdle()
        viewModel.setDevice(AudioDeviceUi.EarPiece)
        verify(exactly = 1) { callMock.setAudioOutputDevice(AudioOutputDevice.Earpiece()) }
    }

    @Test
    fun testSetWiredHeadsetDevice() = runTest {
        advanceUntilIdle()
        viewModel.setDevice(AudioDeviceUi.WiredHeadset)
        verify(exactly = 1) { callMock.setAudioOutputDevice(AudioOutputDevice.WiredHeadset()) }
    }

    @Test
    fun testSetMutedDevice() = runTest {
        advanceUntilIdle()
        viewModel.setDevice(AudioDeviceUi.Muted)
        verify(exactly = 1) { callMock.setAudioOutputDevice(AudioOutputDevice.None()) }
        verify(exactly = 1) { audioMock1.tryDisable() }
        verify(exactly = 1) { audioMock2.tryDisable() }
    }

    @Test
    fun testSetBluetoothDevice() = runTest {
        advanceUntilIdle()
        viewModel.setDevice(AudioDeviceUi.Bluetooth(id = "bluetoothId2", connectionState = BluetoothDeviceState.Disconnected, name = null, batteryLevel = null))
        verify(exactly = 1) { callMock.setAudioOutputDevice(AudioOutputDevice.Bluetooth("bluetoothId2")) }
    }

    @Test
    fun `restore participants audio if previous device was none`() = runTest {
        advanceUntilIdle()
        viewModel.setDevice(AudioDeviceUi.Muted)
        viewModel.setDevice(AudioDeviceUi.LoudSpeaker)
        verify(exactly = 1) { audioMock1.tryEnable() }
        verify(exactly = 1) { audioMock2.tryEnable() }
    }

}