package com.kaleyra.collaboration_suite_phone_ui.call.extensions

import androidx.compose.ui.layout.LayoutCoordinates

internal object LayoutCoordinatesExtensions {

    fun LayoutCoordinates.findRoot(): LayoutCoordinates {
        var root = this
        var parent = root.parentLayoutCoordinates
        while (parent != null) {
            root = parent
            parent = root.parentLayoutCoordinates
        }
        return root
    }
}
