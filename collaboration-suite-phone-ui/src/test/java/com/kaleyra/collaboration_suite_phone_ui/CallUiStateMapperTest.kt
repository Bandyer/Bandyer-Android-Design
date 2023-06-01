package com.kaleyra.collaboration_suite_phone_ui

import android.util.Rational
import com.kaleyra.collaboration_suite.phonebox.Input
import com.kaleyra.collaboration_suite.phonebox.StreamView
import com.kaleyra.collaboration_suite.phonebox.VideoStreamView
import com.kaleyra.collaboration_suite_phone_ui.call.compose.CallUiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.ImmutableView
import com.kaleyra.collaboration_suite_phone_ui.call.compose.StreamUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.VideoUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.CallUiStateMapper.toPipAspectRatio
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class CallUiStateMapperTest {

    private val definitionQualityMock = mockk<Input.Video.Quality.Definition>()

    private val streamStateMock = mockk<StreamView.State.Rendering>()

    private val viewMock = mockk<VideoStreamView>()

    private val video = VideoUi(id = "videoId", view = ImmutableView(viewMock))

    private val stream = StreamUi(id = "streamId", username = "username", video = video)

    private val callUiState = CallUiState(featuredStreams = ImmutableList(listOf(stream)))

    @Before
    fun setUp() {
        every { streamStateMock.definition } returns MutableStateFlow(definitionQualityMock)
        every { viewMock.state } returns MutableStateFlow(streamStateMock)
    }

    @Test
    fun `first featured video has width higher than height, pip aspect ratio is 16-9`() = runTest {
        with(definitionQualityMock) {
            every { width } returns 300
            every { height } returns 200
        }

        val flow = flowOf(callUiState)
        val actual = flow.toPipAspectRatio().first()
        val expected = Rational(16, 9)
        assertEquals(expected, actual)
    }

    @Test
    fun `first featured video has width lower than height, pip aspect ratio is 9-16`() = runTest {
        with(definitionQualityMock) {
            every { width } returns 200
            every { height } returns 300
        }

        val flow = flowOf(callUiState)
        val actual = flow.toPipAspectRatio().first()
        val expected = Rational(9, 16)
        assertEquals(expected, actual)
    }
}