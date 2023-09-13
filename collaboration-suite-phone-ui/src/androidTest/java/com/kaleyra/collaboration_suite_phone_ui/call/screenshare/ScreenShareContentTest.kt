package com.kaleyra.collaboration_suite_phone_ui.screenshare

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.screenshare.model.ScreenShareTargetUi
import com.kaleyra.collaboration_suite_phone_ui.call.screenshare.view.ScreenShareContent
import com.kaleyra.collaboration_suite_phone_ui.common.immutablecollections.ImmutableList
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScreenShareContentTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var items by mutableStateOf(ImmutableList(listOf<ScreenShareTargetUi>()))

    private var screenShareTarget: ScreenShareTargetUi? = null

    @Before
    fun setUp() {
        composeTestRule.setContent {
            ScreenShareContent(
                items = items,
                onItemClick = { screenShareTarget = it }
            )
        }
        screenShareTarget = null
    }

    @After
    fun tearDown() {
        items = ImmutableList(listOf())
        screenShareTarget = null
    }

    @Test
    fun deviceScreenShare_deviceScreenShareItemDisplayed() {
        items = ImmutableList(listOf(ScreenShareTargetUi.Device))
        val fullDevice = composeTestRule.activity.getString(R.string.kaleyra_screenshare_full_device)
        composeTestRule.onNodeWithText(fullDevice).assertIsDisplayed()
    }

    @Test
    fun appScreenShare_appScreenShareItemDisplayed() {
        items = ImmutableList(listOf(ScreenShareTargetUi.Application))
        val appOnly = composeTestRule.activity.getString(R.string.kaleyra_screenshare_app_only)
        composeTestRule.onNodeWithText(appOnly).assertIsDisplayed()
    }

    @Test
    fun userClicksOnItem_onItemClickInvoked() {
        items = ImmutableList(listOf(ScreenShareTargetUi.Device, ScreenShareTargetUi.Application))
        val appOnly = composeTestRule.activity.getString(R.string.kaleyra_screenshare_app_only)
        composeTestRule.onNodeWithText(appOnly).performClick()
        Assert.assertEquals(ScreenShareTargetUi.Application, screenShareTarget)
    }
}