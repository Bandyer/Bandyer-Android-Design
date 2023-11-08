package com.kaleyra.video_sdk.call.pointer

import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.video_sdk.call.pointer.view.Pointer
import com.kaleyra.video_sdk.call.pointer.view.PointerSize
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PointerTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setUp() {
        composeTestRule.setContent {
            Pointer()
        }
    }

    @Test
    fun testPointerSize() {
        composeTestRule.onRoot().assertWidthIsEqualTo(PointerSize)
        composeTestRule.onRoot().assertHeightIsEqualTo(PointerSize)
    }
}