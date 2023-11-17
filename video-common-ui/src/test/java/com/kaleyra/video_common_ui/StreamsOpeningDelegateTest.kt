/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.video_common_ui

import com.kaleyra.video.conference.CallParticipant
import com.kaleyra.video.conference.CallParticipants
import com.kaleyra.video.conference.Stream
import com.kaleyra.video_common_ui.call.StreamsOpeningDelegate
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StreamsOpeningDelegateTest {

    private val streamsOpeningDelegateTest = object : StreamsOpeningDelegate { }

    private val callParticipantsMock = mockk<CallParticipants>(relaxed = true)

    private val meMock = mockk<CallParticipant.Me>(relaxed = true)

    private val participantMock1 = mockk<CallParticipant>(relaxed = true)

    private val participantMock2 = mockk<CallParticipant>(relaxed = true)

    private val myStreamMock = mockk<Stream.Mutable>(relaxed = true)

    private val streamMock1 = mockk<Stream>(relaxed = true)

    private val streamMock2 = mockk<Stream>(relaxed = true)

    @Test
    fun openParticipantsStreams_myStreamsAreOpened() = runTest(UnconfinedTestDispatcher()) {
        every { meMock.streams } returns MutableStateFlow(listOf(myStreamMock))
        every { callParticipantsMock.list } returns listOf(meMock)

        streamsOpeningDelegateTest.openParticipantsStreams(flowOf(callParticipantsMock), backgroundScope)

        verify { myStreamMock.open() }
    }

    @Test
    fun openParticipantsStreams_otherStreamsAreOpened() = runTest(UnconfinedTestDispatcher()) {
        every { participantMock1.streams } returns MutableStateFlow(listOf(streamMock1))
        every { participantMock2.streams } returns MutableStateFlow(listOf(streamMock2))
        every { callParticipantsMock.list } returns listOf(participantMock1, participantMock2)

        streamsOpeningDelegateTest.openParticipantsStreams(flowOf(callParticipantsMock), backgroundScope)

        verify { streamMock1.open() }
        verify { streamMock2.open() }
    }

    @Test
    fun openParticipantsStreams_openIsCalledOnNewStreams() = runTest(UnconfinedTestDispatcher()) {
        val myStreamList = MutableStateFlow(listOf<Stream.Mutable>())
        val otherStreamList = MutableStateFlow(listOf<Stream>())
        every { meMock.streams } returns myStreamList
        every { participantMock2.streams } returns otherStreamList
        every { callParticipantsMock.list } returns listOf(meMock, participantMock2)

        streamsOpeningDelegateTest.openParticipantsStreams(flowOf(callParticipantsMock), backgroundScope)

        myStreamList.value = listOf(myStreamMock)
        otherStreamList.value = listOf(streamMock2)
        verify { myStreamMock.open() }
        verify { streamMock2.open() }
    }

    @Test
    fun openParticipantsStreams_openIsCalledOnNewParticipantStreams() = runTest(UnconfinedTestDispatcher()) {
        every { meMock.streams } returns MutableStateFlow(listOf(myStreamMock))
        every { participantMock2.streams } returns MutableStateFlow(listOf(streamMock1, streamMock2))
        val callParticipantsMock = mockk<CallParticipants> {
            every { list } returns listOf(meMock)
        }
        val participantsFlow = MutableStateFlow(callParticipantsMock)

        streamsOpeningDelegateTest.openParticipantsStreams(participantsFlow, backgroundScope)
        verify { myStreamMock.open() }

        val newCallParticipantsMock = mockk<CallParticipants> {
            every { list } returns listOf(meMock, participantMock2)
        }
        participantsFlow.value = newCallParticipantsMock
        verify { streamMock1.open() }
        verify { streamMock2.open() }
    }
}