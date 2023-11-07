package com.kaleyra.video_sdk.mapper.call

import com.kaleyra.video.conference.Input
import com.kaleyra.video.conference.VideoStreamView
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager.combinedDisplayName
import com.kaleyra.video_sdk.MainDispatcherRule
import com.kaleyra.video_sdk.call.stream.model.ImmutableView
import com.kaleyra.video_sdk.call.pointer.model.PointerUi
import com.kaleyra.video_sdk.call.stream.model.VideoUi
import com.kaleyra.video_sdk.call.mapper.VideoMapper.mapToPointerUi
import com.kaleyra.video_sdk.call.mapper.VideoMapper.mapToPointersUi
import com.kaleyra.video_sdk.call.mapper.VideoMapper.mapToVideoUi
import com.kaleyra.video_sdk.call.mapper.VideoMapper.shouldMirrorPointer
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VideoMapperTest {

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private val viewMock = mockk<VideoStreamView>()

    private val videoMock = mockk<Input.Video.Screen>(relaxed = true)

    private val pointerMock1 = mockk<Input.Video.Event.Pointer>(relaxed = true)

    private val pointerMock2 = mockk<Input.Video.Event.Pointer>(relaxed = true)

    private val lensMock = mockk<Input.Video.Camera.Internal.Lens>(relaxed = true)

    private val cameraMock = mockk<Input.Video.Camera.Internal>(relaxed = true)

    private val usbCameraMock = mockk<Input.Video.Camera.Usb>(relaxed = true)

    @Before
    fun setUp() {
        mockkObject(ContactDetailsManager)
        with(videoMock) {
            every { id } returns "videoId"
            every { view } returns MutableStateFlow(viewMock)
            every { enabled } returns MutableStateFlow(true)
        }
        with(pointerMock1) {
            every { producer } returns mockk {
                every { userId } returns "userId1"
                every { combinedDisplayName } returns MutableStateFlow("displayName")
            }
            every { position.x } returns 30f
            every { position.y } returns 45f
            every { action } returns Input.Video.Event.Pointer.Action.Move
        }
        with(pointerMock2) {
            every { producer } returns mockk {
                every { userId } returns "userId2"
                every { combinedDisplayName } returns MutableStateFlow("displayName2")
            }
            every { position.x } returns 60f
            every { position.y } returns 20f
            every { action } returns Input.Video.Event.Pointer.Action.Move
        }
    }

    @Test
    fun videoInputNull_mapToVideoUi_null() = runTest {
        val actual = MutableStateFlow(null).mapToVideoUi().first()
        Assert.assertEquals(null, actual)
    }

    @Test
    fun videoInputNotNull_mapToVideoUi_mappedVideoUi() = runTest {
        val flow = MutableStateFlow(videoMock)
        val actual = flow.mapToVideoUi().first()
        val expected = VideoUi("videoId", ImmutableView(viewMock), isEnabled = true, isScreenShare = true)
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun videoInputWithNoPointerEvents_mapToPointersUi_emptyList() = runTest {
        val actual = MutableStateFlow(videoMock).mapToPointersUi().first()
        Assert.assertEquals(listOf<PointerUi>(), actual)
    }

    @Test
    fun moveVideoPointerEvent_mapToPointersUi_pointerAddedToList() = runTest {
        val events = MutableSharedFlow<Input.Video.Event.Pointer>()
        every { videoMock.events } returns events

        val expected1 = listOf(PointerUi(pointerMock1.producer.combinedDisplayName.first() ?: "", pointerMock1.position.x, pointerMock1.position.y))
        val expected2 = expected1 + PointerUi(pointerMock2.producer.combinedDisplayName.first() ?: "", pointerMock2.position.x, pointerMock2.position.y)

        val values = mutableListOf<List<PointerUi>>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            MutableStateFlow(videoMock).mapToPointersUi().toList(values)
        }

        assertEquals(listOf<PointerUi>(), values[0])

        events.emit(pointerMock1)
        assertEquals(expected1, values[1])

        events.emit(pointerMock2)
        assertEquals(expected2, values[2])
    }

    @Test
    fun idleVideoPointerEvent_mapToPointerUi_pointerRemovedFromList() = runTest {
        val events = MutableSharedFlow<Input.Video.Event.Pointer>()
        every { videoMock.events } returns events

        val expected1 = listOf(PointerUi(pointerMock1.producer.combinedDisplayName.first() ?: "", pointerMock1.position.x, pointerMock1.position.y), PointerUi(pointerMock2.producer.combinedDisplayName.first() ?: "", pointerMock2.position.x, pointerMock2.position.y))
        val expected2 = listOf(PointerUi(pointerMock1.producer.combinedDisplayName.first() ?: "", pointerMock1.position.x, pointerMock1.position.y))

        val values = mutableListOf<List<PointerUi>>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            MutableStateFlow(videoMock).mapToPointersUi().toList(values)
        }

        assertEquals(listOf<PointerUi>(), values[0])

        events.emit(pointerMock1)
        events.emit(pointerMock2)
        assertEquals(expected1, values[2])

        every { pointerMock2.action } returns Input.Video.Event.Pointer.Action.Idle
        events.emit(pointerMock2)
        assertEquals(expected2, values[3])
    }

    @Test
    fun pointerEventOnInternalBackCameraVideo_mapToPointerUi_pointerIsMirrored() = runTest {
        every { lensMock.isRear } returns true
        every { cameraMock.currentLens } returns MutableStateFlow(lensMock)
        val events = MutableSharedFlow<Input.Video.Event.Pointer>()
        every { cameraMock.events } returns events

        val expected = listOf(PointerUi(pointerMock1.producer.combinedDisplayName.first() ?: "", pointerMock1.position.x, pointerMock1.position.y))

        val values = mutableListOf<List<PointerUi>>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            MutableStateFlow(cameraMock).mapToPointersUi().toList(values)
        }

        assertEquals(listOf<PointerUi>(), values[0])

        events.emit(pointerMock1)
        assertEquals(expected, values[1])
    }

    @Test
    fun pointerEventOnInternalFrontCameraVideo_mapToPointerUi_pointerIsMirrored() = runTest {
        every { lensMock.isRear } returns false
        every { cameraMock.currentLens } returns MutableStateFlow(lensMock)
        val events = MutableSharedFlow<Input.Video.Event.Pointer>()
        every { cameraMock.events } returns events

        val expected = listOf(PointerUi(pointerMock1.producer.combinedDisplayName.first() ?: "", 100 - pointerMock1.position.x, pointerMock1.position.y))

        val values = mutableListOf<List<PointerUi>>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            MutableStateFlow(cameraMock).mapToPointersUi().toList(values)
        }

        assertEquals(listOf<PointerUi>(), values[0])

        events.emit(pointerMock1)
        assertEquals(expected, values[1])
    }

    @Test
    fun pointerEventOnUsbCameraVideo_mapToPointerUi_pointerIsMirrored() = runTest {
        val events = MutableSharedFlow<Input.Video.Event.Pointer>()
        every { usbCameraMock.events } returns events

        val expected = listOf(PointerUi(pointerMock1.producer.combinedDisplayName.first() ?: "", pointerMock1.position.x, pointerMock1.position.y))

        val values = mutableListOf<List<PointerUi>>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            MutableStateFlow(usbCameraMock).mapToPointersUi().toList(values)
        }

        assertEquals(listOf<PointerUi>(), values[0])

        events.emit(pointerMock1)
        assertEquals(expected, values[1])
    }

    @Test
    fun videoPointerEvent_mapToPointerUi_mappedPointerUi() = runTest {
        val actual = pointerMock1.mapToPointerUi()
        val expected = PointerUi(
            username = pointerMock1.producer.combinedDisplayName.first() ?: "",
            x = pointerMock1.position.x,
            y = pointerMock1.position.y
        )
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun mirrorTrue_mapToPointerUi_mirroredPointerUi() = runTest {
        val actual = pointerMock1.mapToPointerUi(mirror = true)
        val expected = PointerUi(
            username = pointerMock1.producer.combinedDisplayName.first() ?: "",
            x = 100 - pointerMock1.position.x,
            y = pointerMock1.position.y
        )
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun internalFrontCamera_shouldMirrorPointer_true() = runTest {
        every { lensMock.isRear } returns false
        every { cameraMock.currentLens } returns MutableStateFlow(lensMock)
        val events = MutableSharedFlow<Input.Video.Event.Pointer>()
        every { cameraMock.events } returns events

        val result = flowOf(cameraMock).shouldMirrorPointer()
        assertEquals(true, result.first())
    }

    @Test
    fun internalBackCamera_shouldMirrorPointer_false() = runTest {
        every { lensMock.isRear } returns true
        every { cameraMock.currentLens } returns MutableStateFlow(lensMock)
        val events = MutableSharedFlow<Input.Video.Event.Pointer>()
        every { cameraMock.events } returns events

        val result = flowOf(cameraMock).shouldMirrorPointer()
        assertEquals(false, result.first())
    }

    @Test
    fun externalCamera_shouldMirrorPointer_false() = runTest {
        val events = MutableSharedFlow<Input.Video.Event.Pointer>()
        every { usbCameraMock.events } returns events

        val result = flowOf(usbCameraMock).shouldMirrorPointer()
        assertEquals(false, result.first())
    }
}