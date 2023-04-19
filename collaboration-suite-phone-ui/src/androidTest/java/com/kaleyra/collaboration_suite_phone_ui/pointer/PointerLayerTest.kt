package com.kaleyra.collaboration_suite_phone_ui.pointer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.call.compose.PointerUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.pointer.PointerLayer
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PointerLayerTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val pointer = PointerUi(username = "username", 30f, 45f)

    @Before
    fun setUp() {
        composeTestRule.setContent {
            Box(Modifier.fillMaxSize()) {
                PointerLayer(pointer)
            }
        }
    }

    @Test
    fun usernameIsDisplayed() {
        composeTestRule.onNodeWithText(pointer.username).assertIsDisplayed()
    }

}