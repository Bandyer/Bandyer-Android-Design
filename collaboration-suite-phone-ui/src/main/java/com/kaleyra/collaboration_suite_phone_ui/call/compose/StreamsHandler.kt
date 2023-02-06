package com.kaleyra.collaboration_suite_phone_ui.call.compose

import kotlinx.coroutines.CoroutineScope
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

    private var featuredStreams = listOf<StreamUi>()

    private var thumbnailsStreams = listOf<StreamUi>()

    private val _streamsArrangement = MutableStateFlow(StreamsArrangement(listOf(), listOf()))

    val streamsArrangement = _streamsArrangement.asStateFlow()

    init {
        combine(
            streams,
            nMaxFeatured
        ) { newStreams, nMaxFeatured ->
            mutex.withLock {
                val addedStreams = updateStreams(newStreams).toSet()

                val newStreamsIds = newStreams.map { it.id }
                val removedFeaturedStreams = findRemovedFeaturedStreams(newStreamsIds).toSet()
                val removedThumbnailsStreams = findRemovedThumbnailsStreams(newStreamsIds).toSet()

                val newFeatured = (featuredStreams + thumbnailsStreams + addedStreams - removedFeaturedStreams).take(nMaxFeatured)
                val movedToThumbnails = featuredStreams - removedFeaturedStreams - newFeatured.toSet()
                val newThumbnails = movedToThumbnails + thumbnailsStreams + addedStreams - newFeatured.toSet() - removedThumbnailsStreams

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
                featuredStreams = newFeatured
                thumbnailsStreams = newThumbnails
                _streamsArrangement.value = StreamsArrangement(newFeatured, newThumbnails)
            }
        }
    }

    /**
     * Update the streams currently in featured and thumbnails
     *
     * @param newStreams The new streams list
     * @return List<StreamUi> The added streams
     */
    private fun updateStreams(newStreams: List<StreamUi>): List<StreamUi> {
        val newFeaturedStreams = featuredStreams.toMutableList()
        val newThumbnailsStreams = thumbnailsStreams.toMutableList()
        val addedStreams = mutableListOf<StreamUi>()

        newStreams.forEach { stream ->
            val featuredIndex = featuredStreams.indexOfFirst { it.id == stream.id }
            if (featuredIndex != -1) {
                newFeaturedStreams[featuredIndex] = stream
            }
            else {
                val thumbnailIndex = thumbnailsStreams.indexOfFirst { it.id == stream.id }
                if (thumbnailIndex != -1) newThumbnailsStreams[thumbnailIndex] = stream
                else addedStreams.add(stream)
            }
        }

        featuredStreams = newFeaturedStreams
        thumbnailsStreams = newThumbnailsStreams

        return addedStreams
    }

//    private fun findAddedStreams(newStreams: List<StreamUi>): List<StreamUi> {
//        val featuredStreamsIds = featuredStreams.map { it.id }
//        val thumbnailsStreamsIds = thumbnailsStreams.map { it.id }
//        return newStreams.filterNot { (featuredStreamsIds + thumbnailsStreamsIds).contains(it.id) }
//    }

    private fun findRemovedFeaturedStreams(newStreamsIds: List<String>): List<StreamUi> {
        return featuredStreams.filterNot { newStreamsIds.contains(it.id) }
    }

    private fun findRemovedThumbnailsStreams(newStreamsIds: List<String>): List<StreamUi> {
        return thumbnailsStreams.filterNot { newStreamsIds.contains(it.id) }
    }

}