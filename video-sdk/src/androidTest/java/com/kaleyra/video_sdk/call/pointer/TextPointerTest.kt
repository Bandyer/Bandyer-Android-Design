package com.kaleyra.video_sdk.call.pointer

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.video_sdk.call.pointer.view.TextPointer
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TextPointerTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private var textWidth = 0

    @Before
    fun setUp() {
        composeTestRule.setContent {
            TextPointer(username = "username", onTextWidth = { textWidth = it })
        }
    }

    @Test
    fun usernameIsDisplayed() {
        composeTestRule.onNodeWithText("username").assertIsDisplayed()
    }

    @Test
    fun textWidthIsInvoked() {
        assertNotEquals(0, textWidth)
    }
}