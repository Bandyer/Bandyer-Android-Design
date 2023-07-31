package com.kaleyra.collaboration_suite_phone_ui

import com.bandyer.android_audiosession.model.AudioOutputDevice
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite.phonebox.CallParticipant
import com.kaleyra.collaboration_suite.phonebox.Input
import com.kaleyra.collaboration_suite.phonebox.Stream
import com.kaleyra.collaboration_suite_core_ui.contactdetails.ContactDetailsManager
import com.kaleyra.collaboration_suite_core_ui.contactdetails.ContactDetailsManager.combinedDisplayName
import com.kaleyra.collaboration_suite_core_ui.utils.UsbCameraUtils
import com.kaleyra.collaboration_suite_extension_audio.extensions.AudioOutputConnectionError
import com.kaleyra.collaboration_suite_extension_audio.extensions.CollaborationAudioExtensions
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.InputMapper
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.InputMapper.hasAudio
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.InputMapper.hasUsbCamera
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.InputMapper.isAudioOnly
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.InputMapper.isAudioVideo
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.InputMapper.isMyCameraEnabled
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.InputMapper.isMyMicEnabled
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.InputMapper.isSharingScreen
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.InputMapper.isVideoIncoming
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.InputMapper.toAudioConnectionFailureMessage
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.InputMapper.toMutedMessage
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.InputMapper.toUsbCameraMessage
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.StreamMapper
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.StreamMapper.doIHaveStreams
import com.kaleyra.collaboration_suite_phone_ui.call.compose.screenshare.viewmodel.ScreenShareViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.compose.usermessages.model.AudioConnectionFailureMessage
import com.kaleyra.collaboration_suite_phone_ui.call.compose.usermessages.model.UsbCameraMessage
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class InputMapperTest {

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private val callMock = mockk<Call>()

    private val videoMock = mockk<Input.Video.Camera.Internal>()

    private val audioMock = mockk<Input.Audio>()

    private val streamMock = mockk<Stream.Mutable>()

    private val participantMeMock = mockk<CallParticipant.Me>()

    @Before
    fun setUp() {
        every { callMock.participants } returns MutableStateFlow(mockk {
            every { me } returns participantMeMock
        })
        every { participantMeMock.streams } returns MutableStateFlow(listOf(streamMock))
        with(streamMock) {
            every { video } returns MutableStateFlow(videoMock)
            every { audio } returns MutableStateFlow(audioMock)
        }
    }

    @Test
    fun cameraStreamVideoEnabled_isMyCameraEnabled_true() = runTest {
        every { videoMock.enabled } returns MutableStateFlow(true)
        val call = MutableStateFlow(callMock)
        val result = call.isMyCameraEnabled()
        val actual = result.first()
        Assert.assertEquals(true, actual)
    }

    @Test
    fun cameraStreamVideoDisabled_isMyCameraEnabled_false() = runTest {
        every { videoMock.enabled } returns MutableStateFlow(false)
        val call = MutableStateFlow(callMock)
        val result = call.isMyCameraEnabled()
        val actual = result.first()
        Assert.assertEquals(false, actual)
    }

    @Test
    fun streamAudioEnabled_isMyMicEnabled_true() = runTest {
        every { audioMock.enabled } returns MutableStateFlow(true)
        val call = MutableStateFlow(callMock)
        val result = call.isMyMicEnabled()
        val actual = result.first()
        Assert.assertEquals(true, actual)
    }

    @Test
    fun streamAudioDisable_isMyMicEnabled_false() = runTest {
        every { audioMock.enabled } returns MutableStateFlow(false)
        val call = MutableStateFlow(callMock)
        val result = call.isMyMicEnabled()
        val actual = result.first()
        Assert.assertEquals(false, actual)
    }

    @Test
    fun sharingStreamInStreamList_isSharingScreen_true() = runTest {
        every { streamMock.id } returns ScreenShareViewModel.SCREEN_SHARE_STREAM_ID
        val call = MutableStateFlow(callMock)
        val result = call.isSharingScreen()
        val actual = result.first()
        Assert.assertEquals(true, actual)
    }

    @Test
    fun sharingStreamNotInStreamList_isSharingScreen_false() = runTest {
        every { streamMock.id } returns "id"
        val call = MutableStateFlow(callMock)
        val result = call.isSharingScreen()
        val actual = result.first()
        Assert.assertEquals(false, actual)
    }

    @Test
    fun preferredTypeHasVideoNull_isAudioOnly_true() = runTest {
        every { callMock.extras } returns mockk {
            every { preferredType } returns MutableStateFlow(Call.PreferredType.audioOnly())
        }
        val call = MutableStateFlow(callMock)
        val result = call.isAudioOnly()
        val actual = result.first()
        Assert.assertEquals(true, actual)
    }

    @Test
    fun preferredTypeChanged_isAudioOnly_changes() = runTest {
        val preferredTypeFlow =  MutableStateFlow(Call.PreferredType.audioOnly())
        every { callMock.extras } returns mockk {
            every { preferredType } returns preferredTypeFlow
        }
        val call = MutableStateFlow(callMock)
        val result = call.isAudioOnly()
        val actual = result.first()
        Assert.assertEquals(true, actual)
        preferredTypeFlow.value = Call.PreferredType.audioVideo()
        val new = result.first()
        Assert.assertEquals(false, new)
    }

    @Test
    fun preferredTypeHasVideo_isAudioOnly_false() = runTest {
        every { callMock.extras } returns mockk {
            every { preferredType } returns MutableStateFlow(Call.PreferredType.audioVideo())
        }
        val call = MutableStateFlow(callMock)
        val result = call.isAudioOnly()
        val actual = result.first()
        Assert.assertEquals(false, actual)
    }

    @Test
    fun preferredTypeHasVideoEnabled_isAudioVideo_true() = runTest {
        every { callMock.extras } returns mockk {
            every { preferredType } returns  MutableStateFlow(Call.PreferredType.audioVideo())
        }
        val call = MutableStateFlow(callMock)
        val result = call.isAudioVideo()
        val actual = result.first()
        Assert.assertEquals(true, actual)
    }

    @Test
    fun preferredTypeChanged_isAudioVideo_changes() = runTest {
        val preferredTypeFlow =  MutableStateFlow(Call.PreferredType.audioOnly())
        every { callMock.extras } returns mockk {
            every { preferredType } returns preferredTypeFlow
        }
        val call = MutableStateFlow(callMock)
        val result = call.isAudioVideo()
        val actual = result.first()
        Assert.assertEquals(false, actual)
        preferredTypeFlow.value = Call.PreferredType.audioVideo()
        val new = result.first()
        Assert.assertEquals(true, new)
    }

    @Test
    fun preferredTypeHasVideoDisabled_isAudioVideo_false() = runTest {
        every { callMock.extras } returns mockk {
            every { preferredType } returns  MutableStateFlow(Call.PreferredType.audioUpgradable())
        }
        val call = MutableStateFlow(callMock)
        val result = call.isAudioVideo()
        val actual = result.first()
        Assert.assertEquals(false, actual)
    }

    @Test
    fun preferredTypeHasAudio_hasAudio_true() = runTest {
        every { callMock.extras } returns mockk {
            every { preferredType } returns  MutableStateFlow(Call.PreferredType.audioVideo())
        }
        val call = MutableStateFlow(callMock)
        val result = call.hasAudio()
        val actual = result.first()
        Assert.assertEquals(true, actual)
    }



    @Test
    fun inputAudioRequestMute_toMutedMessage_adminDisplayName() = runTest {
        mockkObject(ContactDetailsManager)
        val event = mockk<Input.Audio.Event.Request.Mute>()
        val producer = mockk<CallParticipant>()
        every { producer.combinedDisplayName } returns MutableStateFlow("username")
        every { event.producer } returns producer
        every { audioMock.events } returns MutableStateFlow(event)
        val call = MutableStateFlow(callMock)
        val result = call.toMutedMessage()
        val actual = result.first()
        Assert.assertEquals("username", actual.admin)
    }

    @Test
    fun usbCameraNotInAvailableInputs_hasUsbCamera_false() = runTest {
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf(mockk<Input.Video.Camera.Internal>()))
        val call = MutableStateFlow(callMock)
        val actual = call.hasUsbCamera().first()
        Assert.assertEquals(false, actual)
    }

    @Test
    fun usbCameraInAvailableInputs_hasUsbCamera_true() = runTest {
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf(mockk<Input.Video.Camera.Usb>()))
        val call = MutableStateFlow(callMock)
        val actual = call.hasUsbCamera().first()
        Assert.assertEquals(true, actual)
    }

    @Test
    fun usbCameraNotInAvailableInputs_toUsbCameraMessage_usbCameraDisconnectedMessage() = runTest {
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf())
        val call = MutableStateFlow(callMock)
        val actual = call.toUsbCameraMessage().first()
        assert(actual is UsbCameraMessage.Disconnected)
    }

    @Test
    fun usbCameraInAvailableInputsAndItIsSupported_toUsbCameraMessage_usbCameraConnectedMessage() = runTest {
        mockkObject(UsbCameraUtils)
        every { UsbCameraUtils.isSupported() } returns true
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf(mockk<Input.Video.Camera.Usb>(relaxed = true)))
        val call = MutableStateFlow(callMock)
        val actual = call.toUsbCameraMessage().first()
        assert(actual is UsbCameraMessage.Connected)
    }

    @Test
    fun usbCameraNotSupported_toUsbCameraMessage_usbCameraNotSupportedMessage() = runTest {
        mockkObject(UsbCameraUtils)
        every { UsbCameraUtils.isSupported() } returns false
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf(mockk<Input.Video.Camera.Usb>(relaxed = true)))
        val call = MutableStateFlow(callMock)
        val actual = call.toUsbCameraMessage().first()
        assert(actual is UsbCameraMessage.NotSupported)
    }

    @Test
    fun audioConnectionGenericError_toAudioConnectionFailureMessage_genericAudioConnectionFailure() = runTest {
        mockkObject(CollaborationAudioExtensions)
        with(CollaborationAudioExtensions) {
            every { callMock.failedAudioOutputDevice } returns MutableStateFlow(AudioOutputConnectionError(AudioOutputDevice.Loudspeaker(), isInSystemCall = false))
        }
        val call = MutableStateFlow(callMock)
        val actual = call.toAudioConnectionFailureMessage().first()
        assert(actual is AudioConnectionFailureMessage.Generic)
    }

    @Test
    fun audioConnectionInCallError_toAudioConnectionFailureMessage_inCallAudioConnectionFailure() = runTest {
        mockkObject(CollaborationAudioExtensions)
        with(CollaborationAudioExtensions) {
            every { callMock.failedAudioOutputDevice } returns MutableStateFlow(AudioOutputConnectionError(AudioOutputDevice.Loudspeaker(), isInSystemCall = true))
        }
        val call = MutableStateFlow(callMock)
        val actual = call.toAudioConnectionFailureMessage().first()
        assert(actual is AudioConnectionFailureMessage.InSystemCall)
    }

    @Test
    fun audioVideoCallAndIHaveStreams_isVideoIncoming_true() = runTest {
        mockkObject(InputMapper)
        mockkObject(StreamMapper)
        val flow = flowOf(callMock)
        every { flow.isAudioVideo() } returns flowOf(true)
        every { flow.doIHaveStreams() } returns flowOf(true)
        val actual = flow.isVideoIncoming().first()
        assertEquals(true, actual)
    }

    @Test
    fun notAudioVideoCallAndIHaveStreams_isVideoIncoming_false() = runTest {
        mockkObject(InputMapper)
        mockkObject(StreamMapper)
        val flow = flowOf(callMock)
        every { flow.isAudioVideo() } returns flowOf(false)
        every { flow.doIHaveStreams() } returns flowOf(true)
        val actual = flow.isVideoIncoming().first()
        assertEquals(false, actual)
    }

    @Test
    fun audioVideoCallAndIHaveNoStreams_isVideoIncoming_false() = runTest {
        mockkObject(InputMapper)
        mockkObject(StreamMapper)
        val flow = flowOf(callMock)
        every { flow.isAudioVideo() } returns flowOf(true)
        every { flow.doIHaveStreams() } returns flowOf(false)
        val actual = flow.isVideoIncoming().first()
        assertEquals(false, actual)
    }
}