package com.kaleyra.collaboration_suite_phone_ui

import android.graphics.Matrix
import android.util.Size
import androidx.compose.ui.unit.IntSize
import com.kaleyra.collaboration_suite.conference.StreamView
import com.kaleyra.collaboration_suite.conference.VideoStreamView
import com.kaleyra.collaboration_suite_phone_ui.call.extensions.StreamViewExtensions.getScale
import com.kaleyra.collaboration_suite_phone_ui.call.extensions.StreamViewExtensions.getSize
import com.kaleyra.collaboration_suite_phone_ui.call.extensions.StreamViewExtensions.getTranslation
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class StreamViewExtensionsTest {

    @Test
    fun testGetSize() = runTest {
        val view = mockk<VideoStreamView>()
        every { view.videoSize } returns MutableStateFlow(Size(300, 200))
        val actual = view.getSize().first()
        assertEquals(IntSize(300, 200), actual)
    }

    @Test
    fun `test get translation with an affine matrix`() = runTest {
        val view = mockk<VideoStreamView>()
        val state = mockk<StreamView.State.Rendering>()
        val matrix = Matrix().apply {
            setTranslate(.5f, .8f)
        }
        every { state.matrix } returns MutableStateFlow(matrix)
        every { view.state } returns MutableStateFlow(state)
        val actual = view.getTranslation().first()
        assertEquals(.5f, actual[0])
        assertEquals(.8f, actual[1])
    }

    @Test
    fun `test get translation with a not affine matrix`() = runTest {
        val view = mockk<VideoStreamView>()
        val state = mockk<StreamView.State.Rendering>()
        val matrix = Matrix().apply {
            setValues(floatArrayOf(.6f, .7f, .8f, .3f, .1f, .2f, .3f, .6f, .4f))
        }
        every { state.matrix } returns MutableStateFlow(matrix)
        every { view.state } returns MutableStateFlow(state)
        val actual = view.getTranslation().first()
        assertEquals(0f, actual[0])
        assertEquals(0f, actual[1])
    }

    @Test
    fun `test get scale with an affine matrix`() = runTest {
        val view = mockk<VideoStreamView>()
        val state = mockk<StreamView.State.Rendering>()
        val matrix = Matrix().apply {
            setScale(2f, 3f)
        }
        every { state.matrix } returns MutableStateFlow(matrix)
        every { view.state } returns MutableStateFlow(state)
        val actual = view.getScale().first()
        assertEquals(2f, actual[0])
        assertEquals(3f, actual[1])
    }

    @Test
    fun `test get scale with a not affine matrix`() = runTest {
        val view = mockk<VideoStreamView>()
        val state = mockk<StreamView.State.Rendering>()
        val matrix = Matrix().apply {
            setValues(floatArrayOf(.6f, .7f, .8f, .3f, .1f, .2f, .3f, .6f, .4f))
        }
        every { state.matrix } returns MutableStateFlow(matrix)
        every { view.state } returns MutableStateFlow(state)
        val actual = view.getScale().first()
        assertEquals(0f, actual[0])
        assertEquals(0f, actual[1])
    }
}