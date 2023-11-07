package com.kaleyra.video_common_ui.mapper

import com.kaleyra.video.conference.Call
import com.kaleyra.video.conference.CallParticipant
import com.kaleyra.video.conference.Input
import com.kaleyra.video.conference.Stream
import com.kaleyra.video_common_ui.call.CameraStreamPublisher
import com.kaleyra.video_common_ui.mapper.InputMapper.hasScreenSharingInput
import com.kaleyra.video_common_ui.mapper.InputMapper.isAnyScreenInputActive
import com.kaleyra.video_common_ui.mapper.InputMapper.isAppScreenInputActive
import com.kaleyra.video_common_ui.mapper.InputMapper.isDeviceScreenInputActive
import com.kaleyra.video_common_ui.mapper.InputMapper.isInputActive
import com.kaleyra.video_common_ui.mapper.InputMapper.toAudio
import com.kaleyra.video_common_ui.mapper.InputMapper.toMuteEvents
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class InputMapperTests {

    private val activeMicrophoneInputMock = mockk<Input.Audio> {
        every { state } returns MutableStateFlow(Input.State.Active)
    }
    private val inactiveMicrophoneInputMock = mockk<Input.Audio> {
        every { state } returns MutableStateFlow(Input.State.Closed)
    }
    private val activeScreenInputMock = mockk<Input.Video.Screen> {
        every { state } returns MutableStateFlow(Input.State.Active)
    }
    private val inactiveScreenInputMock = mockk<Input.Video.Screen> {
        every { state } returns MutableStateFlow(Input.State.Closed)
    }
    private val activeApplicationInputMock = mockk<Input.Video.Application> {
        every { state } returns MutableStateFlow(Input.State.Active)
    }
    private val inactiveApplicationInputMock = mockk<Input.Video.Application> {
        every { state } returns MutableStateFlow(Input.State.Closed)
    }

    private val callMock = mockk<Call>()

    private val callFlow: Flow<Call> = flowOf(callMock)

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
            every { audio } returns MutableStateFlow(audioMock)
        }
    }

    @Test
    fun noInput_isInputActive_false() = runTest {
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf())
        Assert.assertEquals(false, callFlow.isInputActive<Input.Video.Camera>().first())
    }

    @Test
    fun wrongInput_isInputActive_false() = runTest {
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf(activeMicrophoneInputMock))
        Assert.assertEquals(false, callFlow.isInputActive<Input.Video.Camera>().first())
    }

    @Test
    fun expectedInput_isInputActive_true() = runTest {
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf(activeMicrophoneInputMock))
        Assert.assertEquals(true, callFlow.isInputActive<Input.Audio>().first())
    }

    @Test
    fun expectedInputNotActive_isInputActive_false() = runTest {
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf(inactiveMicrophoneInputMock))
        Assert.assertEquals(false, callFlow.isInputActive<Input.Audio>().first())
    }

    @Test
    fun deviceScreenActive_isDeviceScreenInputActive_true() = runTest {
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf(activeScreenInputMock))
        Assert.assertEquals(true, callFlow.isDeviceScreenInputActive().first())
    }

    @Test
    fun deviceScreenNotActive_isDeviceScreenInputActive_false() = runTest {
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf(inactiveScreenInputMock))
        Assert.assertEquals(false, callFlow.isDeviceScreenInputActive().first())
    }

    @Test
    fun applicationScreenActive_isDeviceApplicationInputActive_true() = runTest {
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf(activeApplicationInputMock))
        Assert.assertEquals(true, callFlow.isAppScreenInputActive().first())
    }

    @Test
    fun applicationScreenNotActive_isDeviceApplicationInputActive_false() = runTest {
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf(inactiveApplicationInputMock))
        Assert.assertEquals(false, callFlow.isAppScreenInputActive().first())
    }

    @Test
    fun deviceScreenActive_isAnyScreenInputActive_true()  = runTest {
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf(activeScreenInputMock))
        Assert.assertEquals(true, callFlow.isAnyScreenInputActive().first())
    }

    @Test
    fun allDeviceActive_isAnyScreenInputActive_true()  = runTest {
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf(activeScreenInputMock, activeApplicationInputMock))
        Assert.assertEquals(true, callFlow.isAnyScreenInputActive().first())
    }

    @Test
    fun allDeviceNotActive_isAnyScreenInputActive_false()  = runTest {
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf(inactiveScreenInputMock, inactiveApplicationInputMock))
        Assert.assertEquals(false, callFlow.isAnyScreenInputActive().first())
    }

    @Test
    fun cameraStreamAudio_toAudio_streamAudio() = runTest {
        with(streamMock) {
            every { id } returns CameraStreamPublisher.CAMERA_STREAM_ID
            every { audio } returns MutableStateFlow(audioMock)
        }
        val call = MutableStateFlow(callMock)
        val result = call.toAudio()
        val actual = result.first()
        Assert.assertEquals(audioMock, actual)
    }

    @Test
    fun cameraStreamAudioNull_toAudio_null() = runTest {
        with(streamMock) {
            every { id } returns CameraStreamPublisher.CAMERA_STREAM_ID
            every { audio } returns MutableStateFlow(null)
        }
        val call = MutableStateFlow(callMock)
        val result = call.toAudio()
        val actual = result.first()
        Assert.assertEquals(null, actual)
    }

    @Test
    fun cameraStreamNotFound_toAudio_null() = runTest {
        every { streamMock.id } returns "randomId"
        val call = MutableStateFlow(callMock)
        val result = call.toAudio()
        val actual = result.first()
        Assert.assertEquals(null, actual)
    }

    @Test
    fun inputAudioRequestMute_toMuteEvent_inputEvent() = runTest {
        val event = mockk<Input.Audio.Event.Request.Mute>()
        every { streamMock.id } returns CameraStreamPublisher.CAMERA_STREAM_ID
        every { audioMock.events } returns MutableStateFlow(event)
        val call = MutableStateFlow(callMock)
        val result = call.toMuteEvents()
        val actual = result.first()
        Assert.assertEquals(event, actual)
    }

    @Test
    fun screenShareInAvailableInputs_hasScreenSharingInput_true() = runTest {
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf(mockk<Input.Video.Screen.My>()))
        val result = flowOf(callMock).hasScreenSharingInput()
        val actual = result.first()
        Assert.assertEquals(true, actual)
    }

    @Test
    fun screenShareNotInAvailableInputs_hasScreenSharingInput_false() = runTest {
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf(mockk<Input.Video.Application>()))
        val result = flowOf(callMock).hasScreenSharingInput()
        val actual = result.first()
        Assert.assertEquals(false, actual)
    }

}