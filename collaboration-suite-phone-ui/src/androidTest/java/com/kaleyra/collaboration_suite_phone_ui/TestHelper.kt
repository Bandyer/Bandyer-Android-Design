package com.kaleyra.collaboration_suite_phone_ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.unit.Dp
import androidx.core.view.KeyEventDispatcher.Component
import androidx.test.ext.junit.rules.ActivityScenarioRule

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

internal fun <T: ComponentActivity> AndroidComposeTestRule<ActivityScenarioRule<T>, T>.findBackButton(): SemanticsNodeInteraction {
    val back = activity.getString(R.string.kaleyra_back)
    return onNodeWithContentDescription(back)
}
internal fun <T: ComponentActivity> AndroidComposeTestRule<ActivityScenarioRule<T>, T>.findAvatar(): SemanticsNodeInteraction {
    val avatar = activity.getString(R.string.kaleyra_avatar)
    return onNodeWithContentDescription(avatar)
}