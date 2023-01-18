package com.kaleyra.collaboration_suite_phone_ui

import android.net.Uri
import com.kaleyra.collaboration_suite.phonebox.*
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow

object PhoneBoxMocks {
    val viewMock = mockk<VideoStreamView>()

    val videoMock = mockk<Input.Video.Camera>(relaxed = true) {
        every { id } returns "videoId"
        every { this@mockk.view } returns MutableStateFlow(viewMock)
        every { enabled } returns MutableStateFlow(true)
    }

    val myVideoMock = mockk<Input.Video.Camera.Internal>(relaxed = true) {
        every { id } returns "myVideoId"
        every { this@mockk.view } returns MutableStateFlow(viewMock)
        every { enabled } returns MutableStateFlow(true)
    }

    val streamMock1 = mockk<Stream> {
        every { id } returns "streamId1"
        every { this@mockk.video } returns MutableStateFlow(videoMock)
    }

    val streamMock2 = mockk<Stream> {
        every { id } returns "streamId2"
        every { this@mockk.video } returns MutableStateFlow(videoMock)
    }

    val streamMock3 = mockk<Stream> {
        every { id } returns "streamId3"
        every { this@mockk.video } returns MutableStateFlow(videoMock)
    }

    val mutableStreamMock = mockk<Stream.Mutable> {
        every { id } returns "myStreamId"
        every { this@mockk.video } returns MutableStateFlow(myVideoMock)
    }

    val uriMock = mockk<Uri>()

    val participantMeMock = mockk<CallParticipant.Me> {
        every { userId } returns "userId1"
        every { streams } returns MutableStateFlow(listOf(mutableStreamMock))
        every { displayName } returns MutableStateFlow("myDisplayName")
        every { displayImage } returns MutableStateFlow(uriMock)
    }

    val participantMock1 = mockk<CallParticipant> {
        every { userId } returns "userId1"
        every { streams } returns MutableStateFlow(listOf(streamMock1, streamMock2))
        every { displayName } returns MutableStateFlow("displayName1")
        every { displayImage } returns MutableStateFlow(uriMock)
    }

    val participantMock2 = mockk<CallParticipant> {
        every { userId } returns "userId2"
        every { streams } returns MutableStateFlow(listOf(streamMock3))
        every { displayName } returns MutableStateFlow("displayName2")
        every { displayImage } returns MutableStateFlow(uriMock)
    }

    val callParticipantsMock = mockk<CallParticipants> {
        every { list } returns listOf(participantMock1, participantMock2)
    }

    val recordingMock = mockk<Call.Recording>()

}