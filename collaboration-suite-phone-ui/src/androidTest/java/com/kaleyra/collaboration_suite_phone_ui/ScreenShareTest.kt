package com.kaleyra.collaboration_suite_phone_ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.call.compose.ScreenShare
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

    private var items by mutableStateOf(ImmutableList(listOf<ScreenShare>()))

    private var screenShare: ScreenShare? = null

    @Before
    fun setUp() {
        composeTestRule.setContent { 
            ScreenShare(items = items, onItemClick = { screenShare = it })
        }
    }

    @Test
    fun userClicksOnItem_onItemClickInvoked() {
        items = ImmutableList(
            listOf(
                ScreenShare.DEVICE,
                ScreenShare.APPLICATION
            )
        )
        val appOnly = composeTestRule.activity.getString(R.string.kaleyra_screenshare_app_only)
        composeTestRule.onNodeWithText(appOnly).performClick()
        assertEquals(ScreenShare.APPLICATION, screenShare)
    }

    @Test
    fun deviceScreenShare_deviceScreenShareItemDisplayed() {
        items = ImmutableList(listOf(ScreenShare.DEVICE))
        val fullDevice = composeTestRule.activity.getString(R.string.kaleyra_screenshare_full_device)
        composeTestRule.onNodeWithText(fullDevice).assertIsDisplayed()
    }

    @Test
    fun appScreenShare_appScreenShareItemDisplayed() {
        items = ImmutableList(listOf(ScreenShare.APPLICATION))
        val appOnly = composeTestRule.activity.getString(R.string.kaleyra_screenshare_app_only)
        composeTestRule.onNodeWithText(appOnly).assertIsDisplayed()
    }
}