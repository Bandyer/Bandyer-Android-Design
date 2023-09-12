package com.kaleyra.collaboration_suite_phone_ui.mapper.call

import android.util.Rational
import android.util.Size
import com.kaleyra.collaboration_suite.conference.VideoStreamView
import com.kaleyra.collaboration_suite_phone_ui.call.screen.model.CallUiState
import com.kaleyra.collaboration_suite_phone_ui.call.ImmutableView
import com.kaleyra.collaboration_suite_phone_ui.call.StreamUi
import com.kaleyra.collaboration_suite_phone_ui.call.VideoUi
import com.kaleyra.collaboration_suite_phone_ui.call.mapper.CallUiStateMapper.toPipAspectRatio
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class CallUiStateMapperTest {

    private val viewMock = mockk<VideoStreamView>()

    private val video = VideoUi(id = "videoId", view = ImmutableView(viewMock))

    private val stream = StreamUi(id = "streamId", username = "username", video = video)

    private val callUiState = CallUiState(featuredStreams = ImmutableList(listOf(stream)))

    @Test
    fun `first featured video width and height have greatest common divisor, the aspect ratio is the resolution divided by the divisor`() = runTest {
        every { viewMock.videoSize } returns MutableStateFlow(Size(1920, 1080))
        val flow = flowOf(callUiState)
        val actual = flow.toPipAspectRatio().first()
        val expected = Rational(16, 9)
        assertEquals(expected, actual)
    }

    @Test
    fun `first featured video width and height does not have greatest common divisor, the aspect ratio is 1-1`() = runTest {
        every { viewMock.videoSize } returns MutableStateFlow(Size(234, 433))
        val flow = flowOf(callUiState)
        val actual = flow.toPipAspectRatio().first()
        val expected = Rational(234, 433)
        assertEquals(expected, actual)
    }

    @Test
    fun `first featured video have invalid width or height, the aspect ratio is NaN`() = runTest {
        every { viewMock.videoSize } returns MutableStateFlow(Size(0, 0))
        val flow = flowOf(callUiState)
        val actual = flow.toPipAspectRatio().first()
        val expected = Rational.NaN
        assertEquals(expected, actual)
    }
}