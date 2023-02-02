package com.kaleyra.collaboration_suite_phone_ui

import com.kaleyra.collaboration_suite_phone_ui.call.compose.StreamsHandler
import com.kaleyra.collaboration_suite_phone_ui.call.compose.StreamUi
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StreamsHandlerTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private lateinit var streamsHandler: StreamsHandler

    private val streamsFlow = MutableStateFlow(listOf<StreamUi>())

    private val nMaxFeaturedFlow = MutableStateFlow(0)

    private val streamMock1 = mockk<StreamUi>()

    private val streamMock2 = mockk<StreamUi>()

    private val streamMock3 = mockk<StreamUi>()

    private val streamMock4 = mockk<StreamUi>()

    private val streamMock5 = mockk<StreamUi>()

    private val streamMock6 = mockk<StreamUi>()

    @Before
    fun setUp() {
        streamsHandler = StreamsHandler(
            streams = streamsFlow,
            nMaxFeatured = nMaxFeaturedFlow
        )
    }

    @After
    fun tearDown() {
        streamsFlow.value = listOf()
        nMaxFeaturedFlow.value = DefaultMaxFeatured
    }

    // test che mio stream viene messo nei thumbnail

    @Test
    fun testSortEmptyList() = runTest {
        streamsFlow.value = listOf()
        nMaxFeaturedFlow.value = 1

        val (featuredStreams, thumbnailsStreams) = streamsHandler.streamsArrangement.first()
        assertEquals(0, featuredStreams.size)
        assertEquals(0, thumbnailsStreams.size)
    }

    @Test
    fun testSortFilledList() = runTest {
        val streams = listOf(streamMock1, streamMock2, streamMock3, streamMock4, streamMock5)
        streamsFlow.value = streams

        val (featuredStreams, thumbnailsStreams) = streamsHandler.streamsArrangement.first()
        assertEquals(streams.take(DefaultMaxFeatured), featuredStreams)
        assertEquals(streams.takeLast(streams.size - DefaultMaxFeatured), thumbnailsStreams)
    }

    @Test
    fun testRemoveFeaturedStream() = runTest {
        val streams = listOf(streamMock1, streamMock2, streamMock3, streamMock4)
        streamsFlow.value = streams

        val (featuredStreams, thumbnailsStreams) = streamsHandler.streamsArrangement.first()
        assertEquals(streams.take(DefaultMaxFeatured), featuredStreams)
        assertEquals(streams.takeLast(streams.size - DefaultMaxFeatured), thumbnailsStreams)

        // Remove featured stream
        val newStreams = listOf(streamMock1, streamMock3, streamMock4)
        streamsFlow.value = newStreams

        val (newFeaturedStreams, newThumbnailsStreams) = streamsHandler.streamsArrangement.first()
        assertEquals(newStreams.take(DefaultMaxFeatured), newFeaturedStreams)
        assertEquals(newStreams.takeLast(streams.size - DefaultMaxFeatured), newThumbnailsStreams)
    }

    @Test
    fun testRemoveThumbnailStream() = runTest {
        val streams = listOf(streamMock1, streamMock2, streamMock3, streamMock4)
        streamsFlow.value = streams

        val (featuredStreams, thumbnailsStreams) = streamsHandler.streamsArrangement.first()
        assertEquals(streams.take(DefaultMaxFeatured), featuredStreams)
        assertEquals(streams.takeLast(streams.size - DefaultMaxFeatured), thumbnailsStreams)

        // Remove thumbnail stream
        val newStreams = listOf(streamMock1, streamMock2, streamMock4)
        streamsFlow.value = newStreams

        val (newFeaturedStreams, newThumbnailsStreams) = streamsHandler.streamsArrangement.first()
        assertEquals(newStreams.take(DefaultMaxFeatured), newFeaturedStreams)
        assertEquals(newStreams.takeLast(newStreams.size - DefaultMaxFeatured), newThumbnailsStreams)
    }

    @Test
    fun testReplaceSomeStreams() = runTest {
        val streams = listOf(streamMock1, streamMock2, streamMock3, streamMock4)
        streamsFlow.value = streams

        val (featuredStreams, thumbnailsStreams) = streamsHandler.streamsArrangement.first()
        assertEquals(streams.take(DefaultMaxFeatured), featuredStreams)
        assertEquals(streams.takeLast(streams.size - DefaultMaxFeatured), thumbnailsStreams)

        val newStreams = listOf(streamMock2, streamMock3, streamMock5, streamMock6)
        streamsFlow.value = newStreams

        val (newFeaturedStreams, newThumbnailsStreams) = streamsHandler.streamsArrangement.first()
        assertEquals(newStreams.take(DefaultMaxFeatured), newFeaturedStreams)
        assertEquals(newStreams.takeLast(newStreams.size - DefaultMaxFeatured), newThumbnailsStreams)
    }

    @Test
    fun testOnlyFeaturedStreams() = runTest {
        val streams = listOf(streamMock1, streamMock2)
        streamsFlow.value = streams
        nMaxFeaturedFlow.value = 2
        val (featuredStreams, thumbnailsStreams) = streamsHandler.streamsArrangement.first()
        assertEquals(streams, featuredStreams)
        assertEquals(0, thumbnailsStreams.size)
    }

    @Test
    fun testOnlyThumbnailsStreams() = runTest {
        val streams = listOf(streamMock1, streamMock2)
        streamsFlow.value = streams
        nMaxFeaturedFlow.value = 0
        val (featuredStreams, thumbnailsStreams) = streamsHandler.streamsArrangement.first()
        assertEquals(0, featuredStreams.size)
        assertEquals(streams, thumbnailsStreams)
    }

    @Test
    fun testIncreaseMaxFeaturedStreams() = runTest {
        val streams = listOf(streamMock1, streamMock2, streamMock3, streamMock4, streamMock5)
        val maxFeatured = 2
        streamsFlow.value = streams
        nMaxFeaturedFlow.value = maxFeatured

        val (featuredStreams, thumbnailsStreams) = streamsHandler.streamsArrangement.first()
        assertEquals(streams.take(maxFeatured), featuredStreams)
        assertEquals(streams.takeLast(streams.size - maxFeatured), thumbnailsStreams)

        val newMaxFeatured = 4
        nMaxFeaturedFlow.value = newMaxFeatured
        val (newFeaturedStreams, newThumbnailsStreams) = streamsHandler.streamsArrangement.first()
        assertEquals(streams.take(newMaxFeatured), newFeaturedStreams)
        assertEquals(streams.takeLast(streams.size - newMaxFeatured), newThumbnailsStreams)
    }

    @Test
    fun testDecreaseMaxFeaturedStreams() = runTest {
        val streams = listOf(streamMock1, streamMock2, streamMock3, streamMock4, streamMock5)
        val maxFeatured = 3
        streamsFlow.value = streams
        nMaxFeaturedFlow.value = maxFeatured
        val (featuredStreams, thumbnailsStreams) = streamsHandler.streamsArrangement.first()
        assertEquals(streams.take(maxFeatured), featuredStreams)
        assertEquals(streams.takeLast(streams.size - maxFeatured), thumbnailsStreams)

        val newMaxFeatured = 1
        nMaxFeaturedFlow.value = newMaxFeatured
        val (newFeaturedStreams, newThumbnailsStreams) = streamsHandler.streamsArrangement.first()
        assertEquals(streams.take(newMaxFeatured), newFeaturedStreams)
        assertEquals(streams.takeLast(streams.size - newMaxFeatured), newThumbnailsStreams)
    }

    @Test
    fun testMyStreamIsInThumbnail() = runTest {

    }

    companion object {
        private const val DefaultMaxFeatured = 2
    }
}