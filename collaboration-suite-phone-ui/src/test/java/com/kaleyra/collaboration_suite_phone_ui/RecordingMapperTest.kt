package com.kaleyra.collaboration_suite_phone_ui

import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite_phone_ui.call.compose.RecordingTypeUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.RecordingMapper.isRecording
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.RecordingMapper.toRecordingUi
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
        every { callMock.extras.recording } returns recordingMock
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun recordingTypeNever_toRecordingUi_null() = runTest {
        every { recordingMock.type } returns Call.Recording.Type.Never
        val result = MutableStateFlow( callMock).toRecordingUi()
        Assert.assertEquals(null, result.first())
    }

    @Test
    fun recordingTypeOnConnect_toRecordingUi_onConnect() = runTest {
        every { recordingMock.type } returns Call.Recording.Type.OnConnect
        val result = MutableStateFlow( callMock).toRecordingUi()
        Assert.assertEquals(RecordingTypeUi.OnConnect, result.first())
    }

    @Test
    fun recordingTypeOnDemand_toRecordingUi_onDemand() = runTest {
        every { recordingMock.type } returns Call.Recording.Type.OnDemand
        val result = MutableStateFlow( callMock).toRecordingUi()
        Assert.assertEquals(RecordingTypeUi.OnDemand, result.first())
    }

    @Test
    fun recordingStateStarted_isRecording_true() = runTest {
        every { callMock.extras.recording.state } returns MutableStateFlow(Call.Recording.State.Started)
        val result = MutableStateFlow(callMock).isRecording()
        Assert.assertEquals(true, result.first())
    }

    @Test
    fun recordingStateStopped_isRecording_false() = runTest {
        every { callMock.extras.recording.state } returns MutableStateFlow(Call.Recording.State.Stopped)
        val result = MutableStateFlow(callMock).isRecording()
        Assert.assertEquals(false, result.first())
    }
}