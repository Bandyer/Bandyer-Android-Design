package com.kaleyra.collaboration_suite_phone_ui.ui.call.virtualbackground

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.compose.virtualbackground.model.VirtualBackgroundUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.virtualbackground.view.VirtualBackgroundItem
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config


@RunWith(RobolectricTestRunner::class)
class VirtualBackgroundItemTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var background by mutableStateOf<VirtualBackgroundUi>(VirtualBackgroundUi.None)

    @Before
    fun setUp() {
        composeTestRule.setContent {
            VirtualBackgroundItem(background = background, selected = false)
        }
    }

    @After
    fun tearDown() {
        background = VirtualBackgroundUi.None
    }

    @Test
    fun noVirtualBackground_noVirtualBackgroundTextDisplayed() {
        background = VirtualBackgroundUi.None
        val none = composeTestRule.activity.getString(R.string.kaleyra_virtual_background_none)
        composeTestRule.onNodeWithText(none).assertIsDisplayed()
    }

    @Test
    fun blurVirtualBackground_blurVirtualBackgroundTextDisplayed() {
        background = VirtualBackgroundUi.Blur("id")
        val blur = composeTestRule.activity.getString(R.string.kaleyra_virtual_background_blur)
        composeTestRule.onNodeWithText(blur).assertIsDisplayed()
    }

    @Test
    fun imageVirtualBackground_imageVirtualBackgroundTextDisplayed() {
        background = VirtualBackgroundUi.Image("id")
        val image = composeTestRule.activity.getString(R.string.kaleyra_virtual_background_image)
        composeTestRule.onNodeWithText(image).assertIsDisplayed()
    }
}