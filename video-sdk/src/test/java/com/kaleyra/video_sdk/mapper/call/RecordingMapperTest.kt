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

package com.kaleyra.video_sdk.mapper.call

import com.kaleyra.video.conference.Call
import com.kaleyra.video_sdk.MainDispatcherRule
import com.kaleyra.video_sdk.call.mapper.RecordingMapper.mapToRecordingStateUi
import com.kaleyra.video_sdk.call.mapper.RecordingMapper.mapToRecordingTypeUi
import com.kaleyra.video_sdk.call.mapper.RecordingMapper.toRecordingMessage
import com.kaleyra.video_sdk.call.mapper.RecordingMapper.toRecordingStateUi
import com.kaleyra.video_sdk.call.mapper.RecordingMapper.toRecordingTypeUi
import com.kaleyra.video_sdk.call.mapper.RecordingMapper.toRecordingUi
import com.kaleyra.video_sdk.call.recording.model.RecordingStateUi
import com.kaleyra.video_sdk.call.recording.model.RecordingTypeUi
import com.kaleyra.video_sdk.call.recording.model.RecordingUi
import com.kaleyra.video_sdk.common.usermessages.model.RecordingMessage
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RecordingMapperTest {

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private val callMock = mockk<Call>()

    private val recordingMock = mockk<Call.Recording>()

    @Before
    fun setUp() {
        every { callMock.recording } returns MutableStateFlow(recordingMock)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun recordingTypeNever_toRecordingTypeUi_never() = runTest {
        every { recordingMock.type } returns Call.Recording.Type.Never
        val result = MutableStateFlow(callMock).toRecordingTypeUi()
        Assert.assertEquals(RecordingTypeUi.Never, result.first())
    }

    @Test
    fun recordingTypeOnConnect_toRecordingTypeUi_onConnect() = runTest {
        every { recordingMock.type } returns Call.Recording.Type.OnConnect
        val result = MutableStateFlow(callMock).toRecordingTypeUi()
        Assert.assertEquals(RecordingTypeUi.OnConnect, result.first())
    }

    @Test
    fun recordingTypeOnDemand_toRecordingTypeUi_onDemand() = runTest {
        every { recordingMock.type } returns Call.Recording.Type.OnDemand
        val result = MutableStateFlow(callMock).toRecordingTypeUi()
        Assert.assertEquals(RecordingTypeUi.OnDemand, result.first())
    }

    @Test
    fun recordingStateStarted_toRecordingStateUi_recordingStateUiStarted() = runTest {
        every { recordingMock.state } returns MutableStateFlow(Call.Recording.State.Started)
        val result = MutableStateFlow(callMock).toRecordingStateUi()
        Assert.assertEquals(RecordingStateUi.Started, result.first())
    }

    @Test
    fun recordingStateStopped_toRecordingStateUi_recordingStateUiStopped() = runTest {
        every { recordingMock.state } returns MutableStateFlow(Call.Recording.State.Stopped)
        val result = MutableStateFlow(callMock).toRecordingStateUi()
        Assert.assertEquals(RecordingStateUi.Stopped, result.first())
    }

    @Test
    fun recordingStateError_toRecordingStateUi_recordingStateUiError() = runTest {
        every { recordingMock.state } returns MutableStateFlow(Call.Recording.State.Stopped.Error)
        val result = MutableStateFlow(callMock).toRecordingStateUi()
        Assert.assertEquals(RecordingStateUi.Error, result.first())
    }

    @Test
    fun recordingStateStarted_toRecordingMessage_recordingMessageStarted() = runTest {
        every { recordingMock.state } returns MutableStateFlow(Call.Recording.State.Started)
        val result = MutableStateFlow(callMock).toRecordingMessage()
        assert(result.first() is RecordingMessage.Started)
    }

    @Test
    fun recordingStateStopped_toRecordingMessage_recordingMessageStopped() = runTest {
        every { recordingMock.state } returns MutableStateFlow(Call.Recording.State.Stopped)
        val result = MutableStateFlow(callMock).toRecordingMessage()
        assert(result.first() is RecordingMessage.Stopped)
    }

    @Test
    fun recordingStateError_toRecordingMessage_recordingMessageError() = runTest {
        every { recordingMock.state } returns MutableStateFlow(Call.Recording.State.Stopped.Error)
        val result = MutableStateFlow(callMock).toRecordingMessage()
        assert(result.first() is RecordingMessage.Failed)
    }

    @Test
    fun callRecording_toRecordingUi_mappedRecordingUi() = runTest {
        every { recordingMock.type } returns Call.Recording.Type.OnConnect
        every { recordingMock.state } returns MutableStateFlow(Call.Recording.State.Started)
        val actual = MutableStateFlow(callMock).toRecordingUi()
        val expected = RecordingUi(RecordingTypeUi.OnConnect, RecordingStateUi.Started)
        Assert.assertEquals(expected, actual.first())
    }

    @Test
    fun recordingTypeOnConnect_mapToRecordingTypeUi_recordingTypeUiOnConnect() = runTest {
        val expected = Call.Recording.Type.OnConnect.mapToRecordingTypeUi()
        val actual = RecordingTypeUi.OnConnect
        Assert.assertEquals(actual, expected)
    }

    @Test
    fun recordingTypeOnDemand_mapToRecordingTypeUi_recordingTypeUiOnDemand() = runTest {
        val expected = Call.Recording.Type.OnDemand.mapToRecordingTypeUi()
        val actual = RecordingTypeUi.OnDemand
        Assert.assertEquals(actual, expected)
    }

    @Test
    fun recordingTypeNever_mapToRecordingTypeUi_recordingTypeUiNever() = runTest {
        val expected = Call.Recording.Type.Never.mapToRecordingTypeUi()
        val actual = RecordingTypeUi.Never
        Assert.assertEquals(actual, expected)
    }

    @Test
    fun recordingStateStarted_mapToRecordingStateUi_recordingStateUiStarted() = runTest {
        val expected = Call.Recording.State.Started.mapToRecordingStateUi()
        val actual = RecordingStateUi.Started
        Assert.assertEquals(actual, expected)
    }

    @Test
    fun recordingStateStopped_mapToRecordingStateUi_recordingStateUiStopped() = runTest {
        val expected = Call.Recording.State.Stopped.mapToRecordingStateUi()
        val actual = RecordingStateUi.Stopped
        Assert.assertEquals(actual, expected)
    }

    @Test
    fun recordingStateError_mapToRecordingStateUi_recordingStateUiError() = runTest {
        val expected = Call.Recording.State.Stopped.Error.mapToRecordingStateUi()
        val actual = RecordingStateUi.Error
        Assert.assertEquals(actual, expected)
    }
}