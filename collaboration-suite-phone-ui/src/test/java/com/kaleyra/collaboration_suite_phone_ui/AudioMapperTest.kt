package com.kaleyra.collaboration_suite_phone_ui

import com.bandyer.android_audiosession.model.AudioOutputDevice
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite_core_ui.CallUI
import com.kaleyra.collaboration_suite_extension_audio.extensions.CollaborationAudioExtensions
import com.kaleyra.collaboration_suite_extension_audio.extensions.CollaborationAudioExtensions.audioOutputDevicesList
import com.kaleyra.collaboration_suite_extension_audio.extensions.CollaborationAudioExtensions.currentAudioOutputDevice
import com.kaleyra.collaboration_suite_phone_ui.call.compose.audiooutput.model.AudioDeviceUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.audiooutput.model.BluetoothDeviceState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.AudioMapper.mapToAudioDeviceUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.AudioMapper.mapToBluetoothDeviceState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.AudioMapper.toAudioDevicesUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.AudioMapper.toCurrentAudioDeviceUi
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class AudioMapperTest {

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private val callMock = mockk<CallUI>()

    @Test
    fun emptyAudioOutputList_toAudioDevicesUi_emptyAudioDeviceUiList() = runTest {
        mockkObject(CollaborationAudioExtensions)
        every { any<Call>().audioOutputDevicesList } returns MutableStateFlow(listOf())
        val call = MutableStateFlow(callMock)
        val result = call.toAudioDevicesUi()
        val actual = result.first()
        Assert.assertEquals(listOf<AudioDeviceUi>(), actual)
    }

    @Test
    fun audioOutputList_toAudioDevicesUi_mappedAudioDeviceUiList() = runTest {
        mockkObject(CollaborationAudioExtensions)
        val devices = listOf(AudioOutputDevice.Loudspeaker(), AudioOutputDevice.Earpiece(), AudioOutputDevice.None())
        every { any<Call>().audioOutputDevicesList } returns MutableStateFlow(devices)
        val call = MutableStateFlow(callMock)
        val result = call.toAudioDevicesUi()
        val actual = result.first()
        val expected = listOf<AudioDeviceUi>(AudioDeviceUi.LoudSpeaker, AudioDeviceUi.EarPiece, AudioDeviceUi.Muted)
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun loudSpeakerAudioOutputDevice_toCurrentAudioDeviceUi_loudspeakerAudioDeviceUi() = runTest {
        mockkObject(CollaborationAudioExtensions)
        every { any<Call>().currentAudioOutputDevice } returns MutableStateFlow(AudioOutputDevice.Loudspeaker())
        val call = MutableStateFlow(callMock)
        val result = call.toCurrentAudioDeviceUi()
        val actual = result.first()
        val expected = AudioDeviceUi.LoudSpeaker
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun earPieceAudioOutputDevice_toCurrentAudioDeviceUi_earPieceAudioDeviceUi() = runTest {
        mockkObject(CollaborationAudioExtensions)
        every { any<Call>().currentAudioOutputDevice } returns MutableStateFlow(AudioOutputDevice.Earpiece())
        val call = MutableStateFlow(callMock)
        val result = call.toCurrentAudioDeviceUi()
        val actual = result.first()
        val expected = AudioDeviceUi.EarPiece
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun wiredHeadsetAudioOutputDevice_toCurrentAudioDeviceUi_wiredHeadsetAudioDeviceUi() = runTest {
        mockkObject(CollaborationAudioExtensions)
        every { any<Call>().currentAudioOutputDevice } returns MutableStateFlow(AudioOutputDevice.WiredHeadset())
        val call = MutableStateFlow(callMock)
        val result = call.toCurrentAudioDeviceUi()
        val actual = result.first()
        val expected = AudioDeviceUi.WiredHeadset
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun mutedAudioOutputDevice_toCurrentAudioDeviceUi_mutedAudioDeviceUi() = runTest {
        mockkObject(CollaborationAudioExtensions)
        every { any<Call>().currentAudioOutputDevice } returns MutableStateFlow(AudioOutputDevice.None())
        val call = MutableStateFlow(callMock)
        val result = call.toCurrentAudioDeviceUi()
        val actual = result.first()
        val expected = AudioDeviceUi.Muted
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun bluetoothAudioOutputDevice_toCurrentAudioDeviceUi_bluetoothAudioDeviceUi() = runTest {
        mockkObject(CollaborationAudioExtensions)
        every { any<Call>().currentAudioOutputDevice } returns MutableStateFlow(AudioOutputDevice.Bluetooth())
        val call = MutableStateFlow(callMock)
        val result = call.toCurrentAudioDeviceUi()
        val actual = result.first()?.javaClass
        val expected = AudioDeviceUi.Bluetooth::class.java
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun loudSpeakerAudioOutputDevice_mapToAudioDeviceUi_loudspeakerAudioDeviceUi() = runTest {
        val audioOutputDevice = AudioOutputDevice.Loudspeaker()
        val actual = audioOutputDevice.mapToAudioDeviceUi()
        Assert.assertEquals(AudioDeviceUi.LoudSpeaker, actual)
    }

    @Test
    fun earPieceAudioOutputDevice_mapToAudioDeviceUimapToAudioDeviceUi_earPieceAudioDeviceUi() = runTest {
        val audioOutputDevice = AudioOutputDevice.Earpiece()
        val actual = audioOutputDevice.mapToAudioDeviceUi()
        Assert.assertEquals(AudioDeviceUi.EarPiece, actual)
    }

    @Test
    fun wiredHeadsetAudioOutputDevice_mapToAudioDeviceUi_wiredHeadsetAudioDeviceUi() = runTest {
        val audioOutputDevice = AudioOutputDevice.WiredHeadset()
        val actual = audioOutputDevice.mapToAudioDeviceUi()
        Assert.assertEquals(AudioDeviceUi.WiredHeadset, actual)
    }

    @Test
    fun noneAudioOutputDevice_mapToAudioDeviceUi_mutedAudioDeviceUi() = runTest {
        val audioOutputDevice = AudioOutputDevice.None()
        val actual = audioOutputDevice.mapToAudioDeviceUi()
        Assert.assertEquals(AudioDeviceUi.Muted, actual)
    }

    @Test
    fun bluetoothAudioOutputDevice_mapToAudioDeviceUi_bluetoothAudioDeviceUi() = runTest {
        val audioOutputDevice = AudioOutputDevice.Bluetooth(
            identifier = "id"
        ).apply {
            name = "name"
            bluetoothConnectionStatus = AudioOutputDevice.Bluetooth.BluetoothConnectionStatus.CONNECTED
            batteryLevel = 50
        }
        val actual = audioOutputDevice.mapToAudioDeviceUi()
        val expected = AudioDeviceUi.Bluetooth(
            id = "id",
            name = "name",
            connectionState = BluetoothDeviceState.Connected,
            batteryLevel = 50
        )
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun bluetoothStatusFailed_mapToBluetoothDeviceState_bluetoothDeviceStateFailed() = runTest {
        val btStatus = AudioOutputDevice.Bluetooth.BluetoothConnectionStatus.FAILED
        val actual = btStatus.mapToBluetoothDeviceState()
        Assert.assertEquals(BluetoothDeviceState.Failed, actual)
    }

    @Test
    fun bluetoothStatusActive_mapToBluetoothDeviceState_bluetoothDeviceStateActive() = runTest {
        val btStatus = AudioOutputDevice.Bluetooth.BluetoothConnectionStatus.ACTIVE
        val actual = btStatus.mapToBluetoothDeviceState()
        Assert.assertEquals(BluetoothDeviceState.Active, actual)
    }

    @Test
    fun bluetoothStatusDisconnected_mapToBluetoothDeviceState_bluetoothDeviceStateDisconnected() = runTest {
        val btStatus = AudioOutputDevice.Bluetooth.BluetoothConnectionStatus.DISCONNECTED
        val actual = btStatus.mapToBluetoothDeviceState()
        Assert.assertEquals(BluetoothDeviceState.Disconnected, actual)
    }

    @Test
    fun bluetoothStatusAvailable_mapToBluetoothDeviceState_bluetoothDeviceStateAvailable() = runTest {
        val btStatus = AudioOutputDevice.Bluetooth.BluetoothConnectionStatus.AVAILABLE
        val actual = btStatus.mapToBluetoothDeviceState()
        Assert.assertEquals(BluetoothDeviceState.Available, actual)
    }

    @Test
    fun bluetoothStatusDeactivating_mapToBluetoothDeviceState_bluetoothDeviceStateDeactivating() = runTest {
        val btStatus = AudioOutputDevice.Bluetooth.BluetoothConnectionStatus.DEACTIVATING
        val actual = btStatus.mapToBluetoothDeviceState()
        Assert.assertEquals(BluetoothDeviceState.Deactivating, actual)
    }

    @Test
    fun bluetoothStatusConnecting_mapToBluetoothDeviceState_bluetoothDeviceStateConnecting() = runTest {
        val btStatus = AudioOutputDevice.Bluetooth.BluetoothConnectionStatus.CONNECTING
        val actual = btStatus.mapToBluetoothDeviceState()
        Assert.assertEquals(BluetoothDeviceState.Connecting, actual)
    }

    @Test
    fun bluetoothStatusConnected_mapToBluetoothDeviceState_bluetoothDeviceStateConnected() = runTest {
        val btStatus = AudioOutputDevice.Bluetooth.BluetoothConnectionStatus.CONNECTED
        val actual = btStatus.mapToBluetoothDeviceState()
        Assert.assertEquals(BluetoothDeviceState.Connected, actual)
    }

    @Test
    fun bluetoothStatusActivating_mapToBluetoothDeviceState_bluetoothDeviceStateActivating() = runTest {
        val btStatus = AudioOutputDevice.Bluetooth.BluetoothConnectionStatus.ACTIVATING
        val actual = btStatus.mapToBluetoothDeviceState()
        Assert.assertEquals(BluetoothDeviceState.Activating, actual)
    }

    @Test
    fun bluetoothStatusConnectingAudio_mapToBluetoothDeviceState_bluetoothDeviceStateConnectingAudio() = runTest {
        val btStatus = AudioOutputDevice.Bluetooth.BluetoothConnectionStatus.CONNECTING_AUDIO
        val actual = btStatus.mapToBluetoothDeviceState()
        Assert.assertEquals(BluetoothDeviceState.ConnectingAudio, actual)
    }

    @Test
    fun bluetoothStatusPlayingAudio_mapToBluetoothDeviceState_bluetoothDeviceStatePlayingAudio () = runTest {
        val btStatus = AudioOutputDevice.Bluetooth.BluetoothConnectionStatus.PLAYING_AUDIO
        val actual = btStatus.mapToBluetoothDeviceState()
        Assert.assertEquals(BluetoothDeviceState.PlayingAudio, actual)
    }

}