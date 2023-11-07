package com.kaleyra.video_sdk.ui.call.streams

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertLeftPositionInRootIsEqualTo
import androidx.compose.ui.test.assertTopPositionInRootIsEqualTo
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onParent
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.width
import com.kaleyra.video_sdk.call.stream.view.core.StreamContainer
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner


@RunWith(RobolectricTestRunner::class)
class StreamContainerTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun setUp() {
        composeTestRule.setContent {
            StreamContainer {
                Spacer(modifier = Modifier.testTag("ChildTag"))
            }
        }
    }

    @Test
    fun childIsCentered() {
        val child = composeTestRule.onNodeWithTag("ChildTag")
        val parent = child.onParent()
        child.assertExists()
        child.assertTopPositionInRootIsEqualTo(parent.getBoundsInRoot().height / 2)
        child.assertLeftPositionInRootIsEqualTo(parent.getBoundsInRoot().width / 2)
    }
}