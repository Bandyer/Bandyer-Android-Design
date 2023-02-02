package com.kaleyra.collaboration_suite_phone_ui.call.compose

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal typealias StreamsArrangement = Pair<List<StreamUi>, List<StreamUi>>

internal class StreamsHandler(
    streams: Flow<List<StreamUi>>,
    nMaxFeatured: Flow<Int>,
    private val coroutineScope: CoroutineScope
) {

    private val mutex = Mutex()

    private var featuredStreams = setOf<StreamUi>()

    private var thumbnailsStreams = setOf<StreamUi>()

    private val _streamsArrangement = MutableStateFlow(StreamsArrangement(listOf(), listOf()))

    val streamsArrangement = _streamsArrangement.asStateFlow()

    init {
        combine(
            streams,
            nMaxFeatured
        ) { streamsList, nMaxFeatured ->
            mutex.withLock {
                val streamsSet = streamsList.toSet()
                val added = streamsSet - featuredStreams - thumbnailsStreams
                val removedFeatured = featuredStreams - streamsSet
                val removedThumbnails = thumbnailsStreams - streamsSet
                val newFeatured = (featuredStreams + thumbnailsStreams + added - removedFeatured).take(nMaxFeatured).toSet()
                val movedToThumbnails = featuredStreams - removedFeatured - newFeatured
                val newThumbnails = movedToThumbnails + thumbnailsStreams + added - newFeatured - removedThumbnails
                featuredStreams = newFeatured
                thumbnailsStreams = newThumbnails
                _streamsArrangement.value = StreamsArrangement(featuredStreams.toList(), thumbnailsStreams.toList())
            }
        }.launchIn(coroutineScope)
    }

    fun swapThumbnail(stream: StreamUi) {
        coroutineScope.launch {
            mutex.withLock {
                val streamIndex = thumbnailsStreams.indexOf(stream)
                if (streamIndex == -1) return@launch

                val newFeatured = (listOf(stream) + featuredStreams).take(featuredStreams.size)
                val removedStream = featuredStreams.last()

                val newThumbnails = thumbnailsStreams
                    .toMutableList()
                    .apply { this[streamIndex] = removedStream }
                featuredStreams = newFeatured.toSet()
                thumbnailsStreams = newThumbnails.toSet()
                _streamsArrangement.value = StreamsArrangement(newFeatured, newThumbnails)
            }
        }
    }

}