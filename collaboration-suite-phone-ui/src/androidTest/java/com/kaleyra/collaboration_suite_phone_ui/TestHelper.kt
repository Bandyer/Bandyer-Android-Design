package com.kaleyra.collaboration_suite_phone_ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.*
import androidx.compose.ui.unit.Dp

internal fun SemanticsNodeInteraction.performScrollUp() {
    performTouchInput {
        this.swipe(
            start = this.center,
            end = Offset(this.center.x, this.center.y + 500),
            durationMillis = 200
        )
    }
}

internal fun SemanticsNodeInteraction.assertRightPositionInRootIsEqualTo(
    expectedRight: Dp
): SemanticsNodeInteraction {
    getUnclippedBoundsInRoot().right.assertIsEqualTo(expectedRight, "right")
    return this
}