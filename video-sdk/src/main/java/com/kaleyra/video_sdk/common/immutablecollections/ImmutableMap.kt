package com.kaleyra.video_sdk.common.immutablecollections

import androidx.compose.runtime.Immutable
@Immutable
data class ImmutableMap<K, out V>(val value: Map<K, V> = HashMap()) {
    operator fun get(key: K): V? = value[key]

    fun count() = value.count()
}