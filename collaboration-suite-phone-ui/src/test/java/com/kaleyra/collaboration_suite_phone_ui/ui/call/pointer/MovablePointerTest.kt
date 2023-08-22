package com.kaleyra.collaboration_suite_phone_ui.ui.call.pointer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.IntSize
import com.kaleyra.collaboration_suite_phone_ui.call.compose.PointerUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.pointer.MovablePointer
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class MovablePointerTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val pointer = PointerUi(username = "username", 30f, 45f)

    @Before
    fun setUp() {
        composeTestRule.setContent {
            var size by remember { mutableStateOf(IntSize(0, 0)) }
            Box(
                Modifier
                    .fillMaxSize()
                    .onGloballyPositioned { size = it.size }
            ) {
                MovablePointer(pointer, size, floatArrayOf(.5f, .5f))
            }
        }
    }

    @Test
    fun usernameIsDisplayed() {
        composeTestRule.onNodeWithText(pointer.username).assertIsDisplayed()
    }

}