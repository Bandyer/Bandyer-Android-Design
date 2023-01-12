package com.kaleyra.collaboration_suite_phone_ui

import android.net.Uri
import com.kaleyra.collaboration_suite.phonebox.*
import com.kaleyra.collaboration_suite_phone_ui.call.compose.CallModelMapper.mapToVideoUi
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
        val inputs = mockk<Inputs>(relaxed = true) {
            every { availableInputs } returns MutableStateFlow(setOf(video))
            every { releaseAll() } returns Unit
        }
        val uri = mockk<Uri>()
        val me = mockk<CallParticipant.Me> {
            every { displayName } returns MutableStateFlow("displayName")
            every { displayImage } returns MutableStateFlow(uri)
        }
        val participants = mockk<CallParticipants> {
            every { this@mockk.me } returns me
        }
        every { callMock.inputs } returns inputs
        every { callMock.participants } returns MutableStateFlow(participants)

        advanceUntilIdle()

//        val stream = flowOf(callMock).mapToMyStreamUi(this)
//        val actual = stream.take(1).first()
//        val expected = StreamUi(video = null, username = "displayName", avatar = ImmutableUri(uri))
//        val expected = StreamUi(video = null, username = "", avatar = null)
//        assertEquals(expected, actual)
//        advanceUntilIdle()
//
//        val actual2 = stream.take(1).first()
//        val expected2 = StreamUi(video =  VideoUi("videoId", view, true), username = "displayName", avatar = ImmutableUri(uri))
//        assertEquals(expected2, actual2)
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