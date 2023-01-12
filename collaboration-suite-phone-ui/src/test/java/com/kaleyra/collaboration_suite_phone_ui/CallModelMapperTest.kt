package com.kaleyra.collaboration_suite_phone_ui

import android.net.Uri
import com.kaleyra.collaboration_suite.phonebox.*
import com.kaleyra.collaboration_suite_phone_ui.call.compose.CallModelMapper.mapToStreamUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.CallModelMapper.mapToVideoUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.ImmutableUri
import com.kaleyra.collaboration_suite_phone_ui.call.compose.StreamUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.VideoUi
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class CallModelMapperTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testMapToMyStreamUi() = runTest {
        val view = mockk<VideoStreamView>()
        val video = mockk<Input.Video.Camera>(relaxed = true) {
            every { id } returns "videoId"
            every { this@mockk.view } returns MutableStateFlow(view)
            every { enabled } returns MutableStateFlow(true)
        }
        val stream = mockk<Stream> {
            every { this@mockk.video } returns MutableStateFlow(video)
        }
        val uri = mockk<Uri>()
        val displayName = MutableStateFlow("displayName")
        val displayImage = MutableStateFlow(uri)

        val flow = MutableStateFlow(stream)
        val actual = flow.mapToStreamUi(displayName, displayImage).first()
        val expected = StreamUi(
            video = VideoUi(id = "videoId", view = view, isEnabled = true),
            username = "displayName",
            avatar = ImmutableUri(uri)
        )
        assertEquals(expected, actual)
    }

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