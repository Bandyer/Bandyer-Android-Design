package com.kaleyra.collaboration_suite_phone_ui

import com.kaleyra.collaboration_suite_phone_ui.call.compose.StreamSorter.sortStreams
import com.kaleyra.collaboration_suite_phone_ui.call.compose.StreamUi
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class StreamSorterTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    // test update stream dopo update max featured
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
        val (featuredStreams, thumbnailsStreams) = flow.sortStreams(1).first()
        assertEquals(0, featuredStreams.size)
        assertEquals(0, thumbnailsStreams.size)
    }

    @Test
    fun testSortFilledList() = runTest {
        val streams = listOf(streamMock1, streamMock2, streamMock3, streamMock4, streamMock5)
        val flow = MutableStateFlow(streams)
        val (featuredStreams, thumbnailsStreams) = flow.sortStreams(2).first()
        assertEquals(streams.take(2), featuredStreams)
        assertEquals(streams.takeLast(3), thumbnailsStreams)
    }

    @Test
    fun testRemoveFeaturedStream() = runTest {
        val streams = listOf(streamMock1, streamMock2, streamMock3, streamMock4)
        val flow = MutableStateFlow(streams)
        val result = flow.sortStreams(2)

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
        val result = flow.sortStreams(2)

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
        val result = flow.sortStreams(2)

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
    fun testOneStream_streamIsFeatured() = runTest {
        val streams = listOf(streamMock1)
        val flow = MutableStateFlow(streams)
        val result = flow.sortStreams(2)
        val (featuredStreams, _) = result.first()
        assertEquals(streams, featuredStreams)
        assertEquals(0, featuredStreams.size)
    }

    @Test
    fun testTwoStream_oneIsFeatured_oneIsThumbnails() = runTest {
        val streams = listOf(streamMock1)
        val flow = MutableStateFlow(streams)
        val result = flow.sortStreams(2)
        val (featuredStreams, thumbnailsStreams) = result.first()
        assertEquals(streams, featuredStreams)
        assertEquals(0, thumbnailsStreams.size)
    }

    @Test
    fun testMyStreamIsInThumbnail() = runTest {

    }
}