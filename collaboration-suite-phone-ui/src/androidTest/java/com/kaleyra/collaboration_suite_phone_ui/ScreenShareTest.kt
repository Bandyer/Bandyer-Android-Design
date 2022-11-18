package com.kaleyra.collaboration_suite_phone_ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.call.compose.model.ScreenShareTarget
import com.kaleyra.collaboration_suite_phone_ui.call.compose.screenshare.ScreenShare
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScreenShareTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var items by mutableStateOf(ImmutableList(listOf<ScreenShareTarget>()))

    private var screenShareTarget: ScreenShareTarget? = null

    private var closeClicked = false

    @Before
    fun setUp() {
        composeTestRule.setContent { 
            ScreenShare(items = items, onItemClick = { screenShareTarget = it }, onCloseClick = { closeClicked = true })
        }
    }

    @Test
    fun screenShareTitleDisplayed() {
        val title = composeTestRule.activity.getString(R.string.kaleyra_screenshare_picker_title)
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
    }

    @Test
    fun userClicksClose_onCloseClickInvoked() {
        val close = composeTestRule.activity.getString(R.string.kaleyra_close)
        composeTestRule.onNodeWithContentDescription(close).performClick()
        assert(closeClicked)
    }

    @Test
    fun userClicksOnItem_onItemClickInvoked() {
        items = ImmutableList(
            listOf(
                ScreenShareTarget.Device,
                ScreenShareTarget.Application
            )
        )
        val appOnly = composeTestRule.activity.getString(R.string.kaleyra_screenshare_app_only)
        composeTestRule.onNodeWithText(appOnly).performClick()
        assertEquals(ScreenShareTarget.Application, screenShareTarget)
    }

    @Test
    fun deviceScreenShare_deviceScreenShareItemDisplayed() {
        items = ImmutableList(listOf(ScreenShareTarget.Device))
        val fullDevice = composeTestRule.activity.getString(R.string.kaleyra_screenshare_full_device)
        composeTestRule.onNodeWithText(fullDevice).assertIsDisplayed()
    }

    @Test
    fun appScreenShare_appScreenShareItemDisplayed() {
        items = ImmutableList(listOf(ScreenShareTarget.Application))
        val appOnly = composeTestRule.activity.getString(R.string.kaleyra_screenshare_app_only)
        composeTestRule.onNodeWithText(appOnly).assertIsDisplayed()
    }
}