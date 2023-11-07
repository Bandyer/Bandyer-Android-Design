package com.kaleyra.video_sdk

import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_sdk.call.mapper.InputMapper
import com.kaleyra.video_sdk.call.mapper.RecordingMapper
import com.kaleyra.video_sdk.common.usermessages.model.AudioConnectionFailureMessage
import com.kaleyra.video_sdk.common.usermessages.model.CameraRestrictionMessage
import com.kaleyra.video_sdk.common.usermessages.model.MutedMessage
import com.kaleyra.video_sdk.common.usermessages.model.RecordingMessage
import com.kaleyra.video_sdk.common.usermessages.model.UsbCameraMessage
import com.kaleyra.video_sdk.common.usermessages.provider.CallUserMessagesProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CallUserMessagesProviderTest {

    @get:Rule
    val mainDispatcherRule = com.kaleyra.video_sdk.MainDispatcherRule()

    private val callFlow = flowOf(mockk<CallUI>(relaxed = true))

    @Before
    fun setUp() {
        mockkObject(InputMapper)
        mockkObject(RecordingMapper)
    }

    @After
    fun tearDown() {
        unmockkAll()
        CallUserMessagesProvider.dispose()
    }

    @Test
    fun testStart() = runTest {
        CallUserMessagesProvider.start(callFlow, backgroundScope)
        assertEquals(true, backgroundScope.isActive)
    }

    @Test
    fun testDoubleStart() = runTest {
        val scope = MainScope()
        CallUserMessagesProvider.start(callFlow, scope)
        CallUserMessagesProvider.start(callFlow, backgroundScope)
        assertEquals(false, scope.isActive)
        assertEquals(true, backgroundScope.isActive)
    }

    @Test
    fun testDispose() = runTest {
        CallUserMessagesProvider.start(callFlow, backgroundScope)
        CallUserMessagesProvider.dispose()
        assertEquals(false, backgroundScope.isActive)
    }

    @Test
    fun testRecordingStartedUserMessage() = runTest {
        with(RecordingMapper) {
            every { callFlow.toRecordingMessage() } returns flowOf(RecordingMessage.Started)
        }
        CallUserMessagesProvider.start(callFlow, backgroundScope)
        val actual = CallUserMessagesProvider.userMessage.first()
        assert(actual is RecordingMessage.Started)
    }

    @Test
    fun recordingStateInitializedWithStopped_recordingStoppedUserMessageNotReceived() = runTest {
        with(RecordingMapper) {
            every { callFlow.toRecordingMessage() } returns flowOf(RecordingMessage.Stopped)
        }
        CallUserMessagesProvider.start(callFlow, backgroundScope)
        val result = withTimeoutOrNull(100) {
            CallUserMessagesProvider.userMessage.first()
        }
        assertEquals(null, result)
    }

    @Test
    fun testRecordingStoppedUserMessage() = runTest {
        val messageFlow = MutableStateFlow<RecordingMessage>(RecordingMessage.Stopped)
        with(RecordingMapper) {
            every { callFlow.toRecordingMessage() } returns messageFlow
        }
        CallUserMessagesProvider.start(callFlow, backgroundScope)
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            val actual = CallUserMessagesProvider.userMessage.drop(1).first()
            assert(actual is RecordingMessage.Stopped)
        }
        messageFlow.value = RecordingMessage.Started
    }

    @Test
    fun testRecordingFailedUserMessage() = runTest {
        with(RecordingMapper) {
            every { callFlow.toRecordingMessage() } returns flowOf(RecordingMessage.Failed)
        }
        CallUserMessagesProvider.start(callFlow, backgroundScope)
        val actual = CallUserMessagesProvider.userMessage.first()
        assert(actual is RecordingMessage.Failed)
    }

    @Test
    fun testMutedUserMessage() = runTest {
        with(InputMapper) {
            every { callFlow.toMutedMessage() } returns flowOf(MutedMessage(null))
        }
        CallUserMessagesProvider.start(callFlow, backgroundScope)
        withTimeout(100) {
            CallUserMessagesProvider.userMessage.first()
        }
    }

    @Test
    fun testUsbConnectedUserMessage() = runTest {
        with(InputMapper) {
            every { callFlow.toUsbCameraMessage() } returns flowOf(UsbCameraMessage.Connected(""))
        }
        CallUserMessagesProvider.start(callFlow, backgroundScope)
        val actual = CallUserMessagesProvider.userMessage.first()
        assert(actual is UsbCameraMessage.Connected)
    }

    @Test
    fun usbInitiallyDisconnected_usbDisconnectedUserMessageNotReceived() = runTest {
        with(InputMapper) {
            every { callFlow.toUsbCameraMessage() } returns flowOf(UsbCameraMessage.Disconnected)
        }
        CallUserMessagesProvider.start(callFlow, backgroundScope)
        val result = withTimeoutOrNull(100) {
            CallUserMessagesProvider.userMessage.first()
        }
        assertEquals(null, result)
    }

    @Test
    fun testUsbDisconnectedUserMessage() = runTest {
        val messageFlow = MutableStateFlow<UsbCameraMessage>(UsbCameraMessage.Disconnected)
        with(InputMapper) {
            every { callFlow.toUsbCameraMessage() } returns messageFlow
        }
        CallUserMessagesProvider.start(callFlow, backgroundScope)
        backgroundScope.launch(UnconfinedTestDispatcher()) {
            val actual = CallUserMessagesProvider.userMessage.drop(1).first()
            assert(actual is UsbCameraMessage.Disconnected)
        }
        messageFlow.value = UsbCameraMessage.Connected("")
    }

    @Test
    fun testUsbNotSupportedUserMessage() = runTest {
        with(InputMapper) {
            every { callFlow.toUsbCameraMessage() } returns flowOf(UsbCameraMessage.NotSupported)
        }
        CallUserMessagesProvider.start(callFlow, backgroundScope)
        val actual = CallUserMessagesProvider.userMessage.first()
        assert(actual is UsbCameraMessage.NotSupported)
    }

    @Test
    fun testGenericAudioOutputFailureMessage() = runTest {
        with(InputMapper) {
            every { callFlow.toAudioConnectionFailureMessage() } returns flowOf(
                AudioConnectionFailureMessage.Generic)
        }
        CallUserMessagesProvider.start(callFlow, backgroundScope)
        val actual = CallUserMessagesProvider.userMessage.first()
        assert(actual is AudioConnectionFailureMessage.Generic)
    }

    @Test
    fun testInSystemCallAudioOutputFailureMessage() = runTest {
        with(InputMapper) {
            every { callFlow.toAudioConnectionFailureMessage() } returns flowOf(
                AudioConnectionFailureMessage.InSystemCall)
        }
        CallUserMessagesProvider.start(callFlow, backgroundScope)
        val actual = CallUserMessagesProvider.userMessage.first()
        assert(actual is AudioConnectionFailureMessage.InSystemCall)
    }

    @Test
    fun testSendUserMessage() = runTest {
        CallUserMessagesProvider.start(callFlow, backgroundScope)
        CallUserMessagesProvider.sendUserMessage(CameraRestrictionMessage())
        val actual = CallUserMessagesProvider.userMessage.first()
        assert(actual is CameraRestrictionMessage)
    }
}