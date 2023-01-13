package com.kaleyra.collaboration_suite_phone_ui

import android.net.Uri
import com.kaleyra.collaboration_suite.phonebox.*
import com.kaleyra.collaboration_suite_phone_ui.call.compose.CallModelMapper.mapToStreamsUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.CallModelMapper.mapToVideoUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.ImmutableUri
import com.kaleyra.collaboration_suite_phone_ui.call.compose.StreamUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.VideoUi
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class CallModelMapperTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    @Test
    fun testMapToStreamsUi() = runTest {
        val view = mockk<VideoStreamView>()
        val video = mockk<Input.Video.Camera>(relaxed = true) {
            every { id } returns "videoId"
            every { this@mockk.view } returns MutableStateFlow(view)
            every { enabled } returns MutableStateFlow(true)
        }
        val stream1 = mockk<Stream> {
            every { id } returns "streamId1"
            every { this@mockk.video } returns MutableStateFlow(video)
        }
        val stream2 = mockk<Stream> {
            every { id } returns "streamId2"
            every { this@mockk.video } returns MutableStateFlow(video)
        }
        val stream3 = mockk<Stream> {
            every { id } returns "streamId3"
            every { this@mockk.video } returns MutableStateFlow(video)
        }
        val uri = mockk<Uri>()
        val displayName = MutableStateFlow("displayName")
        val displayImage = MutableStateFlow(uri)

        val streams = MutableStateFlow(listOf(stream1, stream2, stream3))
        val result = streams.mapToStreamsUi(displayName, displayImage)
        val actual = result.first()
        val expected = listOf(
            StreamUi(
                id = "streamId1",
                video = VideoUi(id = "videoId", view = view, isEnabled = true),
                username = "displayName",
                avatar = ImmutableUri(uri)
            ),
            StreamUi(
                id = "streamId2",
                video = VideoUi(id = "videoId", view = view, isEnabled = true),
                username = "displayName",
                avatar = ImmutableUri(uri)
            ),
            StreamUi(
                id = "streamId3",
                video = VideoUi(id = "videoId", view = view, isEnabled = true),
                username = "displayName",
                avatar = ImmutableUri(uri)
            )
        )
        assertEquals(expected, actual)
        streams.value = listOf(stream1)
        val expected2 = listOf(
            StreamUi(
                id = "streamId1",
                video = VideoUi(id = "videoId", view = view, isEnabled = true),
                username = "displayName",
                avatar = ImmutableUri(uri)
            )
        )
        assertEquals(expected2, result.first())
    }

//    @OptIn(ExperimentalCoroutinesApi::class)
//    @Test
//    fun testMapToMyStreamUi() = runTest {
//        val view = mockk<VideoStreamView>()
//        val video = mockk<Input.Video.Camera>(relaxed = true) {
//            every { id } returns "videoId"
//            every { this@mockk.view } returns MutableStateFlow(view)
//            every { enabled } returns MutableStateFlow(true)
//        }
//        val stream = mockk<Stream> {
//            every { this@mockk.video } returns MutableStateFlow(video)
//        }
//        val uri = mockk<Uri>()
//        val displayName = MutableStateFlow("displayName")
//        val displayImage = MutableStateFlow(uri)
//
//        val flow = MutableStateFlow(stream)
//        val actual = flow.mapToStreamUi(displayName, displayImage).first()
//        val expected = StreamUi(
//            video = VideoUi(id = "videoId", view = view, isEnabled = true),
//            username = "displayName",
//            avatar = ImmutableUri(uri)
//        )
//        assertEquals(expected, actual)
//    }

    @Test
    fun videoInputNull_mapToVideoUi_null() = runTest {
        val actual = MutableStateFlow(null).mapToVideoUi().first()
        assertEquals(null, actual)
    }

    @Test
    fun videoInput_mapToVideoUi_videoUi() = runTest {
        val view = mockk<VideoStreamView>()
        val video = mockk<Input.Video.Camera>(relaxed = true) {
            every { id } returns "videoId"
            every { this@mockk.view } returns MutableStateFlow(view)
            every { enabled } returns MutableStateFlow(true)
        }

        val flow = MutableStateFlow(video)
        val actual = flow.mapToVideoUi().first()
        val expected = VideoUi("videoId", view, true)
        assertEquals(expected, actual)
    }
}