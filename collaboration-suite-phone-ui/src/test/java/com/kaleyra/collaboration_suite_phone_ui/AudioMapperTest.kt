package com.kaleyra.collaboration_suite_phone_ui

import com.bandyer.android_audiosession.model.AudioOutputDevice
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite_core_ui.CallUI
import com.kaleyra.collaboration_suite_extension_audio.extensions.CollaborationAudioExtensions
import com.kaleyra.collaboration_suite_extension_audio.extensions.CollaborationAudioExtensions.currentAudioOutputDevice
import com.kaleyra.collaboration_suite_phone_ui.call.compose.audiooutput.model.AudioDeviceUi
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
    fun loudSpeakerAudioOutputDevice_toCurrentAudioDeviceUi_loudspeakerAudioDeviceUi() = runTest {
        mockkObject(CollaborationAudioExtensions)
        every { any<Call>().currentAudioOutputDevice } returns MutableStateFlow(AudioOutputDevice.LOUDSPEAKER())
        val call = MutableStateFlow(callMock)
        val result = call.toCurrentAudioDeviceUi()
        val actual = result.first()
        val expected = AudioDeviceUi.LoudSpeaker
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun earPieceAudioOutputDevice_toCurrentAudioDeviceUi_earPieceAudioDeviceUi() = runTest {
        mockkObject(CollaborationAudioExtensions)
        every { any<Call>().currentAudioOutputDevice } returns MutableStateFlow(AudioOutputDevice.EARPIECE())
        val call = MutableStateFlow(callMock)
        val result = call.toCurrentAudioDeviceUi()
        val actual = result.first()
        val expected = AudioDeviceUi.EarPiece
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun wiredHeadsetAudioOutputDevice_toCurrentAudioDeviceUi_wiredHeadsetAudioDeviceUi() = runTest {
        mockkObject(CollaborationAudioExtensions)
        every { any<Call>().currentAudioOutputDevice } returns MutableStateFlow(AudioOutputDevice.WIRED_HEADSET())
        val call = MutableStateFlow(callMock)
        val result = call.toCurrentAudioDeviceUi()
        val actual = result.first()
        val expected = AudioDeviceUi.WiredHeadset
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun mutedAudioOutputDevice_toCurrentAudioDeviceUi_mutedAudioDeviceUi() = runTest {
        mockkObject(CollaborationAudioExtensions)
        every { any<Call>().currentAudioOutputDevice } returns MutableStateFlow(AudioOutputDevice.NONE())
        val call = MutableStateFlow(callMock)
        val result = call.toCurrentAudioDeviceUi()
        val actual = result.first()
        val expected = AudioDeviceUi.Muted
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun bluetoothAudioOutputDevice_toCurrentAudioDeviceUi_bluetoothAudioDeviceUi() = runTest {
        mockkObject(CollaborationAudioExtensions)
        every { any<Call>().currentAudioOutputDevice } returns MutableStateFlow(AudioOutputDevice.BLUETOOTH())
        val call = MutableStateFlow(callMock)
        val result = call.toCurrentAudioDeviceUi()
        val actual = result.first()?.javaClass
        val expected = AudioDeviceUi.Bluetooth::class.java
        Assert.assertEquals(expected, actual)
    }
}