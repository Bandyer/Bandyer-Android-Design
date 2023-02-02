package com.kaleyra.collaboration_suite_phone_ui

import com.kaleyra.collaboration_suite_phone_ui.call.compose.StreamSorter.sortStreams
import com.kaleyra.collaboration_suite_phone_ui.call.compose.StreamUi
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StreamSorterTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    // test che mio stream viene messo nei thumbnail

    private val streamMock1 = mockk<StreamUi>()
    private val streamMock2 = mockk<StreamUi>()
    private val streamMock3 = mockk<StreamUi>()
    private val streamMock4 = mockk<StreamUi>()
    private val streamMock5 = mockk<StreamUi>()
    private val streamMock6 = mockk<StreamUi>()

    @Test
    fun testSortEmptyList() = runTest {
        val streams = listOf<StreamUi>()
        val flow = MutableStateFlow(streams)
        val (featuredStreams, thumbnailsStreams) = sortStreams(flow, flowOf(1)).first()
        assertEquals(0, featuredStreams.size)
        assertEquals(0, thumbnailsStreams.size)
    }

    @Test
    fun testSortFilledList() = runTest {
        val streams = listOf(streamMock1, streamMock2, streamMock3, streamMock4, streamMock5)
        val flow = MutableStateFlow(streams)
        val (featuredStreams, thumbnailsStreams) = sortStreams(flow, flowOf(2)).first()
        assertEquals(streams.take(2), featuredStreams)
        assertEquals(streams.takeLast(3), thumbnailsStreams)
    }

    @Test
    fun testRemoveFeaturedStream() = runTest {
        val streams = listOf(streamMock1, streamMock2, streamMock3, streamMock4)
        val flow = MutableStateFlow(streams)
        val result = sortStreams(flow, flowOf(2))

        val (featuredStreams, thumbnailsStreams) = result.first()
        assertEquals(streams.take(2), featuredStreams)
        assertEquals(streams.takeLast(2), thumbnailsStreams)

        // Remove featured stream
        val newStreams = listOf(streamMock1, streamMock3, streamMock4)
        flow.value = newStreams

        val (newFeaturedStreams, newThumbnailsStreams) = result.first()
        assertEquals(newStreams.take(2), newFeaturedStreams)
        assertEquals(newStreams.takeLast(1), newThumbnailsStreams)
    }

    @Test
    fun testRemoveThumbnailStream() = runTest {
        val streams = listOf(streamMock1, streamMock2, streamMock3, streamMock4)
        val flow = MutableStateFlow(streams)
        val result = sortStreams(flow, flowOf(2))

        val (featuredStreams, thumbnailsStreams) = result.first()
        assertEquals(streams.take(2), featuredStreams)
        assertEquals(streams.takeLast(2), thumbnailsStreams)

        // Remove thumbnail stream
        val newStreams = listOf(streamMock1, streamMock2, streamMock4)
        flow.value = newStreams

        val (newFeaturedStreams, newThumbnailsStreams) = result.first()
        assertEquals(newStreams.take(2), newFeaturedStreams)
        assertEquals(newStreams.takeLast(1), newThumbnailsStreams)
    }

    @Test
    fun testReplaceSomeStreams() = runTest {
        val streams = listOf(streamMock1, streamMock2, streamMock3, streamMock4)
        val flow = MutableStateFlow(streams)
        val result = sortStreams(flow, flowOf(2))

        val (featuredStreams, thumbnailsStreams) = result.first()
        assertEquals(streams.take(2), featuredStreams)
        assertEquals(streams.takeLast(2), thumbnailsStreams)

        val newStreams = listOf(streamMock2, streamMock3, streamMock5, streamMock6)
        flow.value = newStreams

        val (newFeaturedStreams, newThumbnailsStreams) = result.first()
        assertEquals(newStreams.take(2), newFeaturedStreams)
        assertEquals(newStreams.takeLast(2), newThumbnailsStreams)
    }

    @Test
    fun testOnlyFeaturedStreams() = runTest {
        val streams = listOf(streamMock1, streamMock2)
        val flow = MutableStateFlow(streams)
        val result = sortStreams(flow, flowOf(2))
        val (featuredStreams, thumbnailsStreams) = result.first()
        assertEquals(streams, featuredStreams)
        assertEquals(0, thumbnailsStreams.size)
    }

    @Test
    fun testOnlyThumbnailsStreams() = runTest {
        val streams = listOf(streamMock1, streamMock2)
        val flow = MutableStateFlow(streams)
        val result = sortStreams(flow, flowOf(0))
        val (featuredStreams, thumbnailsStreams) = result.first()
        assertEquals(0, featuredStreams.size)
        assertEquals(streams, thumbnailsStreams)
    }

    @Test
    fun testIncreaseMaxFeaturedStreams() = runTest {
        val streams = listOf(streamMock1, streamMock2, streamMock3, streamMock4, streamMock5)
        val nMaxFeatured = MutableStateFlow(2)
        val result = sortStreams(
            streams = MutableStateFlow(streams),
            nMaxFeatured = nMaxFeatured
        )
        val (featuredStreams, thumbnailsStreams) = result.first()
        assertEquals(streams.take(2), featuredStreams)
        assertEquals(streams.takeLast(3), thumbnailsStreams)

        nMaxFeatured.value = 4
        val (newFeaturedStreams, newThumbnailsStreams) = result.first()
        assertEquals(streams.take(4), newFeaturedStreams)
        assertEquals(streams.takeLast(1), newThumbnailsStreams)
    }

    @Test
    fun testDecreaseMaxFeaturedStreams() = runTest {
        val streams = listOf(streamMock1, streamMock2, streamMock3, streamMock4, streamMock5)
        val nMaxFeatured = MutableStateFlow(3)
        val result = sortStreams(
            streams = MutableStateFlow(streams),
            nMaxFeatured = nMaxFeatured
        )
        val (featuredStreams, thumbnailsStreams) = result.first()
        assertEquals(streams.take(3), featuredStreams)
        assertEquals(streams.takeLast(2), thumbnailsStreams)

        nMaxFeatured.value = 1
        val (newFeaturedStreams, newThumbnailsStreams) = result.first()
        assertEquals(streams.take(1), newFeaturedStreams)
        assertEquals(streams.takeLast(4), newThumbnailsStreams)
    }

    @Test
    fun testMyStreamIsInThumbnail() = runTest {

    }
}