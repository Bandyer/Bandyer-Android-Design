package com.kaleyra.collaboration_suite_phone_ui.common.immutablecollections

import androidx.compose.runtime.Immutable

// Needed for compose stability to avoid recomposition
// Tried kotlinx-collections-immutable but they were not working properly
@Immutable
data class ImmutableList<out T>(val value: List<T> = listOf()) {
    fun getOrNull(index: Int) = value.getOrNull(index)
    fun count() = value.count()
}