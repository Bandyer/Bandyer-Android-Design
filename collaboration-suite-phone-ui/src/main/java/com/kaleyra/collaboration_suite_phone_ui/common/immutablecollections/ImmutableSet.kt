package com.kaleyra.collaboration_suite_phone_ui.common.immutablecollections

import androidx.compose.runtime.Immutable

@Immutable
data class ImmutableSet<out T>(val value: Set<T>) {
    fun count() = value.count()
}