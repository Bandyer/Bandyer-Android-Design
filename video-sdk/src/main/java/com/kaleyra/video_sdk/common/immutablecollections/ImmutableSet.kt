package com.kaleyra.video_sdk.common.immutablecollections

import androidx.compose.runtime.Immutable

@Immutable
data class ImmutableSet<out T>(val value: Set<T> = setOf()) {
    fun count() = value.count()
}