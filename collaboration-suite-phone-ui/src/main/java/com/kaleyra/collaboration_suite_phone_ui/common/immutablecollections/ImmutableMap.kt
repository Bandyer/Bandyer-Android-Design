package com.kaleyra.collaboration_suite_phone_ui.common.immutablecollections

import androidx.compose.runtime.Immutable
@Immutable
data class ImmutableMap<K, out V>(val value: Map<K, V>) {
    fun count() = value.count()
}