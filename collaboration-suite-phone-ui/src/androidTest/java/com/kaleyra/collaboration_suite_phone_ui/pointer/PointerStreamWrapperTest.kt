package com.kaleyra.collaboration_suite_phone_ui.pointer

import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.IntSize
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.call.compose.PointerUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.pointer.MovablePointerTag
import com.kaleyra.collaboration_suite_phone_ui.call.compose.pointer.PointerStreamWrapper
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PointerStreamWrapperTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val pointerList = ImmutableList<PointerUi>(listOf(mockk(relaxed = true), mockk(relaxed = true), mockk(relaxed = true)))

    @Before
    fun setUp() {
        composeTestRule.setContent {
            PointerStreamWrapper(pointerList = pointerList) {
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