package com.kaleyra.collaboration_suite_phone_ui.ui.call.pointer

import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import com.kaleyra.collaboration_suite_phone_ui.call.pointer.model.PointerUi
import com.kaleyra.collaboration_suite_phone_ui.call.pointer.view.MovablePointerTag
import com.kaleyra.collaboration_suite_phone_ui.call.pointer.view.PointerStreamWrapper
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PointerStreamWrapperTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val pointerList = ImmutableList<PointerUi>(listOf(mockk(relaxed = true), mockk(relaxed = true), mockk(relaxed = true)))

    @Before
    fun setUp() {
        composeTestRule.setContent {
            PointerStreamWrapper(streamView = null , pointerList = pointerList, isTesting = true) {
                Spacer(modifier = Modifier.testTag("ChildTag"))
            }
        }
    }

    @Test
    fun streamComposableDoesExists() {
        composeTestRule.onNodeWithTag("ChildTag").assertExists()
    }

    @Test
    fun checkPointerLayersCount() {
        composeTestRule.onAllNodesWithTag(MovablePointerTag).assertCountEquals(pointerList.count())
    }

}