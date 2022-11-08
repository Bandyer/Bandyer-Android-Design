package com.kaleyra.collaboration_suite_phone_ui.call.compose.utility

import androidx.compose.ui.layout.LayoutCoordinates

internal fun LayoutCoordinates.findRoot(): LayoutCoordinates {
    var root = this
    var parent = root.parentLayoutCoordinates
    while (parent != null) {
        root = parent
        parent = root.parentLayoutCoordinates
    }
    return root
}