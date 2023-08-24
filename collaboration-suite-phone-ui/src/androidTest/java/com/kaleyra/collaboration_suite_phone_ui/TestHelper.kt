package com.kaleyra.collaboration_suite_phone_ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.unit.Dp
import androidx.test.ext.junit.rules.ActivityScenarioRule

internal fun SemanticsNodeInteraction.performDoubleClick() {
    performTouchInput {
        doubleClick()
    }
}

internal fun SemanticsNodeInteraction.performScrollUp() {
    performTouchInput {
        this.swipe(
            start = this.center,
            end = Offset(this.center.x, this.center.y + 500),
            durationMillis = 200
        )
    }
}

internal fun SemanticsNodeInteraction.performVerticalSwipe(amount: Float) {
    performTouchInput {
        val startY = top
        val endY = top - amount * height
        swipe(
            start = Offset(center.x, startY),
            end = Offset(center.x, endY),
            durationMillis = 200
        )
    }
}

internal fun SemanticsNodeInteraction.performHorizontalSwipe(amount: Float) {
    performTouchInput {
        val startX = left
        val endX = left - amount * width
        swipe(
            start = Offset(startX, center.y),
            end = Offset(endX, center.y),
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

internal fun <T: ComponentActivity> AndroidComposeTestRule<ActivityScenarioRule<T>, T>.findBackButton(): SemanticsNodeInteraction {
    val back = activity.getString(R.string.kaleyra_back)
    return onNodeWithContentDescription(back)
}
internal fun <T: ComponentActivity> AndroidComposeTestRule<ActivityScenarioRule<T>, T>.findAvatar(): SemanticsNodeInteraction {
    val avatar = activity.getString(R.string.kaleyra_avatar)
    return onNodeWithContentDescription(avatar)
}