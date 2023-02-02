package com.kaleyra.collaboration_suite_phone_ui.call.compose

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

object StreamSorter {

    fun sortStreams(
        streams: Flow<List<StreamUi>>,
        nMaxFeatured: Flow<Int>
    ): Flow<Pair<List<StreamUi>, List<StreamUi>>> {
        var featuredStreams = setOf<StreamUi>()
        var thumbnailsStreams = setOf<StreamUi>()
        return combine(
            streams,
            nMaxFeatured
        ) { streamsList, nMaxFeatured ->
            val streamsSet = streamsList.toSet()
            val added = streamsSet - featuredStreams - thumbnailsStreams
            val removedFeatured = featuredStreams - streamsSet
            val removedThumbnails = thumbnailsStreams - streamsSet
            val newFeatured = (featuredStreams + thumbnailsStreams + added - removedFeatured).take(nMaxFeatured).toSet()
            val movedToThumbnails = featuredStreams - removedFeatured - newFeatured
            val newThumbnails = movedToThumbnails + thumbnailsStreams + added - newFeatured - removedThumbnails
            featuredStreams = newFeatured
            thumbnailsStreams = newThumbnails
            Pair(featuredStreams.toList(), thumbnailsStreams.toList())
        }
    }

}