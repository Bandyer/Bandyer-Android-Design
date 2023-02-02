package com.kaleyra.collaboration_suite_phone_ui.call.compose

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object StreamSorter {
    fun Flow<List<StreamUi>>.sortStreams(
        maxFeatured: Int
    ): Flow<Pair<List<StreamUi>, List<StreamUi>>> {
        var featuredStreams = listOf<StreamUi>()
        var thumbnailsStreams = listOf<StreamUi>()
        return map { streams ->
            val added = streams - featuredStreams.toSet() - thumbnailsStreams.toSet()
            val removedFeatured = featuredStreams - streams.toSet()
            val removedThumbnails = thumbnailsStreams - streams.toSet()
            val newFeatured = (featuredStreams + thumbnailsStreams + added - removedFeatured.toSet()).take(maxFeatured)
            val newThumbnails = thumbnailsStreams + added - newFeatured.toSet() - removedThumbnails.toSet()
            featuredStreams = newFeatured
            thumbnailsStreams = newThumbnails
            Pair(featuredStreams, thumbnailsStreams)
        }
    }
}