package com.kaleyra.video_sdk.call.stream.arrangement

import com.kaleyra.video_sdk.call.stream.model.StreamUi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal typealias StreamsArrangement = Pair<List<StreamUi>, List<StreamUi>>

internal class StreamsHandler(
    streams: Flow<List<StreamUi>>,
    nOfMaxFeatured: Flow<Int>,
    private val coroutineScope: CoroutineScope
) {

    companion object {
        const val STREAMS_HANDLER_UPDATE_DEBOUNCE = 100L
    }

    private val mutex = Mutex()

    private var featuredStreams = listOf<StreamUi>()

    private var thumbnailsStreams = listOf<StreamUi>()

    private val _streamsArrangement = MutableStateFlow(StreamsArrangement(listOf(), listOf()))

    val streamsArrangement = _streamsArrangement.asStateFlow()

    init {
        combine(
            streams.debounce(STREAMS_HANDLER_UPDATE_DEBOUNCE),
            nOfMaxFeatured
        ) { newStreams, nOfMaxFeatured ->
            mutex.withLock {
                val addedStreams = updateStreams(newStreams).toSet()
                val addedScreenShareStreams =
                    addedStreams.filter { it.video?.isScreenShare == true }.toSet()
                val cameraStreams = addedStreams - addedScreenShareStreams

                val newStreamsIds = newStreams.map { it.id }
                val removedFeaturedStreams = findRemovedFeaturedStreams(newStreamsIds).toSet()
                val removedThumbnailsStreams = findRemovedThumbnailsStreams(newStreamsIds).toSet()

                var newFeaturedStreams =
                    (addedScreenShareStreams + featuredStreams + cameraStreams - removedFeaturedStreams + thumbnailsStreams - removedThumbnailsStreams).take(
                        nOfMaxFeatured
                    )
                val movedToThumbnails =
                    featuredStreams - removedFeaturedStreams - newFeaturedStreams.toSet()
                var newThumbnailsStreams =
                    movedToThumbnails + thumbnailsStreams + addedStreams - newFeaturedStreams.toSet() - removedThumbnailsStreams

                // There must be at least a thumbnail stream
                if (newThumbnailsStreams.isEmpty() && newFeaturedStreams.size > 1) {
                    newThumbnailsStreams = listOf(newFeaturedStreams.last())
                    newFeaturedStreams = newFeaturedStreams - newThumbnailsStreams.toSet()
                }

                featuredStreams = newFeaturedStreams
                thumbnailsStreams = newThumbnailsStreams
                _streamsArrangement.value =
                    StreamsArrangement(featuredStreams.toList(), thumbnailsStreams.toList())
            }
        }.launchIn(coroutineScope)
    }

    fun swapThumbnail(streamId: String) {
        coroutineScope.launch {
            mutex.withLock {
                val streamIndex = thumbnailsStreams.indexOfFirst { it.id == streamId }
                if (streamIndex == -1) return@launch

                val stream = thumbnailsStreams[streamIndex]
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
     * Update the streams currently in featured and thumbnails and return the new added streams
     *
     * @param newStreams The new streams list
     * @return List<StreamUi> The added streams
     */
    private fun updateStreams(newStreams: List<StreamUi>): List<StreamUi> {
        // Reset the streams arrangement if there was only one stream before
        if (newStreams.size > 1 && thumbnailsStreams.isEmpty()) {
            featuredStreams = listOf()
            thumbnailsStreams = listOf()
        }

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

    private fun findRemovedFeaturedStreams(newStreamsIds: List<String>): List<StreamUi> {
        return featuredStreams.filterNot { newStreamsIds.contains(it.id) }
    }

    private fun findRemovedThumbnailsStreams(newStreamsIds: List<String>): List<StreamUi> {
        return thumbnailsStreams.filterNot { newStreamsIds.contains(it.id) }
    }

}