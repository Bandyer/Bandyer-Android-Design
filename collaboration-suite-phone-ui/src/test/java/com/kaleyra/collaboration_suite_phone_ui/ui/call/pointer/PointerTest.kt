package com.kaleyra.collaboration_suite_phone_ui.ui.call.pointer

import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.kaleyra.collaboration_suite_phone_ui.call.compose.pointer.Pointer
import com.kaleyra.collaboration_suite_phone_ui.call.compose.pointer.PointerSize
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
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