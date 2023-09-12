package com.kaleyra.collaboration_suite_phone_ui.call.virtualbackground

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.component.virtualbackground.model.VirtualBackgroundUi
import com.kaleyra.collaboration_suite_phone_ui.call.component.virtualbackground.view.VirtualBackgroundContent
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class VirtualBackgroundContentTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var items by mutableStateOf(ImmutableList(listOf<VirtualBackgroundUi>()))

    private var backgroundClicked: VirtualBackgroundUi? = null

    @Before
    fun setUp() {
        composeTestRule.setContent {
            VirtualBackgroundContent(
                items = items,
                currentBackground = VirtualBackgroundUi.None,
                onItemClick = { backgroundClicked = it }
            )
        }
    }

    @After
    fun tearDown() {
        items = ImmutableList(listOf())
        backgroundClicked = null
    }

    @Test
    fun noVirtualBackground_noVirtualBackgroundItemDisplayed() {
        items = ImmutableList(listOf(VirtualBackgroundUi.None))
        val none = composeTestRule.activity.getString(R.string.kaleyra_virtual_background_none)
        composeTestRule.onNodeWithText(none).assertIsDisplayed()
    }

    @Test
    fun blurVirtualBackground_blurVirtualBackgroundItemDisplayed() {
        items = ImmutableList(listOf(VirtualBackgroundUi.Blur("id")))
        val blur = composeTestRule.activity.getString(R.string.kaleyra_virtual_background_blur)
        composeTestRule.onNodeWithText(blur).assertIsDisplayed()
    }

    @Test
    fun imageVirtualBackground_imageVirtualBackgroundItemDisplayed() {
        items = ImmutableList(listOf(VirtualBackgroundUi.Image("id")))
        val image = composeTestRule.activity.getString(R.string.kaleyra_virtual_background_image)
        composeTestRule.onNodeWithText(image).assertIsDisplayed()
    }

    @Test
    fun userClicksOnItem_onItemClickInvoked() {
        items = ImmutableList(listOf(VirtualBackgroundUi.None, VirtualBackgroundUi.Blur("id")))
        val blur = composeTestRule.activity.getString(R.string.kaleyra_virtual_background_blur)
        composeTestRule.onNodeWithText(blur).performClick()
        Assert.assertEquals(VirtualBackgroundUi.Blur("id"), backgroundClicked)
    }
}

