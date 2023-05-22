package com.kaleyra.collaboration_suite_phone_ui

import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite.phonebox.CallParticipant
import com.kaleyra.collaboration_suite.phonebox.Input
import com.kaleyra.collaboration_suite.phonebox.Stream
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.InputMapper.hasAudio
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.InputMapper.hasBeenMutedBy
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.InputMapper.hasVideo
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.InputMapper.isMyCameraEnabled
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.InputMapper.isMyMicEnabled
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.InputMapper.isSharingScreen
import com.kaleyra.collaboration_suite_phone_ui.call.compose.screenshare.viewmodel.ScreenShareViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert
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
    fun preferredTypeHasVideoNull_hasVideo_false() = runTest {
        every { callMock.extras } returns mockk {
            every { preferredType } returns Call.PreferredType(video = null)
        }
        val call = MutableStateFlow(callMock)
        val result = call.hasVideo()
        val actual = result.first()
        Assert.assertEquals(false, actual)
    }

    @Test
    fun preferredTypeHasVideo_hasVideo_true() = runTest {
        every { callMock.extras } returns mockk {
            every { preferredType } returns Call.PreferredType()
        }
        val call = MutableStateFlow(callMock)
        val result = call.hasVideo()
        val actual = result.first()
        Assert.assertEquals(true, actual)
    }

    @Test
    fun preferredTypeHasAudioNull_hasAudio_false() = runTest {
        every { callMock.extras } returns mockk {
            every { preferredType } returns Call.PreferredType(audio = null)
        }
        val call = MutableStateFlow(callMock)
        val result = call.hasAudio()
        val actual = result.first()
        Assert.assertEquals(false, actual)
    }

    @Test
    fun preferredTypeHasAudio_hasAudio_true() = runTest {
        every { callMock.extras } returns mockk {
            every { preferredType } returns Call.PreferredType()
        }
        val call = MutableStateFlow(callMock)
        val result = call.hasAudio()
        val actual = result.first()
        Assert.assertEquals(true, actual)
    }

    @Test
    fun inputAudioRequestMute_hasBeenMutedBy_adminDisplayName() = runTest {
        val event = mockk<Input.Audio.Event.Request.Mute>()
        val producer = mockk<CallParticipant>()
        every { producer.displayName } returns MutableStateFlow("username")
        every { event.producer } returns producer
        every { audioMock.events } returns MutableStateFlow(event)
        val call = MutableStateFlow(callMock)
        val result = call.hasBeenMutedBy()
        val actual = result.first()
        val expected = "username"
        Assert.assertEquals(expected, actual)
    }
}