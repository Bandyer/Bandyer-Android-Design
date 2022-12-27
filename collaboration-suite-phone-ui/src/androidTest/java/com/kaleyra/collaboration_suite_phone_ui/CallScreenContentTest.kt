package com.kaleyra.collaboration_suite_phone_ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.call.compose.*
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.callInfoMock
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CallScreenContentTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var callInfo by mutableStateOf(callInfoMock)

    private var isBackPressed = false

    private var isFullscreenClicked: StreamUi? = null

    @Before
    fun setUp() {
        composeTestRule.setContent {
            CallScreenContent(
                state = rememberCallScreenContentState(
                    streams = ImmutableList(listOf(streamUiMock, streamUiMock)),
                    callInfo = callInfo,
                    configuration = LocalConfiguration.current,
                    maxWidth = 400.dp
                ),
                onBackPressed = { isBackPressed = true },
                onFullscreenClick = { isFullscreenClicked = it }
            )
        }
        isBackPressed = false
        isFullscreenClicked = null
    }

    @Test
    fun showCallInfoFalse_callInfoWidgetDoesNotExists() {
        showCallInfo = false
        callInfo = callInfoMock.copy(subtitle = "subtitle")
        composeTestRule.onNodeWithText("subtitle").assertDoesNotExist()
    }

    @Test
    fun showCallInfoTrue_callInfoWidgetIsDisplayed() {
        showCallInfo = true
        callInfo = callInfoMock.copy(subtitle = "subtitle")
        composeTestRule.onNodeWithText("subtitle").assertIsDisplayed()
    }

    @Test
    fun userClicksCallInfoWidgetBackButton_onBackPressedInvoked() {
        val back = composeTestRule.activity.getString(R.string.kaleyra_back)
        composeTestRule.onNodeWithContentDescription(back).performClick()
        assert(isBackPressed)
    }

    @Test
    fun userClicksStreamBackButton_onBackPressedInvoked() {
        showCallInfo = false
        val back = composeTestRule.activity.getString(R.string.kaleyra_back)
        composeTestRule.onNodeWithContentDescription(back).performClick()
        assert(isBackPressed)
    }

    @Test
    fun callInfoIsDisplayedRightToBackButtonWhenWatermarkNull() {
        callInfo = callInfoMock.copy(watermarkInfo = null, subtitle = "subtitle")
        val back = composeTestRule.activity.getString(R.string.kaleyra_back)
        val subtitleBounds = composeTestRule.onNodeWithText("subtitle").getBoundsInRoot()
        val backBounds = composeTestRule.onNodeWithContentDescription(back).getBoundsInRoot()
        assert(subtitleBounds.left > backBounds.right)
    }

    @Test
    fun userClicksEnterFullscreen_fullscreenStreamIsDisplayed() {
        val enterFullscreen = composeTestRule.activity.getString(R.string.kaleyra_enter_fullscreen)
        val exitFullscreen = composeTestRule.activity.getString(R.string.kaleyra_exit_fullscreen)
        composeTestRule.onAllNodesWithContentDescription(enterFullscreen).onFirst().performClick()
        composeTestRule.onNodeWithText("user1").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(exitFullscreen).assertIsDisplayed()
        // check there is only one StreamTile?
    }

    @Test
    fun pressExitFullscreen_returnToTileViews() {
        val enterFullscreen = composeTestRule.activity.getString(R.string.kaleyra_enter_fullscreen)
        val exitFullscreen = composeTestRule.activity.getString(R.string.kaleyra_exit_fullscreen)
        composeTestRule.onAllNodesWithContentDescription(enterFullscreen).onFirst().performClick()
        composeTestRule.onNodeWithContentDescription(exitFullscreen).performClick()
        composeTestRule.onAllNodesWithContentDescription(enterFullscreen).assertCountEquals(2)
    }

    @Test
    fun pressExitFullscreen_returnToTileViews2() {
        showCallInfo = true
        val enterFullscreen = composeTestRule.activity.getString(R.string.kaleyra_enter_fullscreen)
        val exitFullscreen = composeTestRule.activity.getString(R.string.kaleyra_exit_fullscreen)
        val back = composeTestRule.activity.getString(R.string.kaleyra_back)
        composeTestRule.onAllNodesWithContentDescription(enterFullscreen).assertCountEquals(2)
        composeTestRule.onAllNodesWithContentDescription(enterFullscreen).onFirst().performClick()
        composeTestRule.onAllNodesWithContentDescription(exitFullscreen).assertCountEquals(1)
        composeTestRule.onNodeWithContentDescription(back).performClick()
        composeTestRule.onAllNodesWithContentDescription(enterFullscreen).assertCountEquals(2)
    }

    @Test
    fun pressExitFullscreen_returnToTileViews3() {
        showCallInfo = false
        val enterFullscreen = composeTestRule.activity.getString(R.string.kaleyra_enter_fullscreen)
        val exitFullscreen = composeTestRule.activity.getString(R.string.kaleyra_exit_fullscreen)
        val back = composeTestRule.activity.getString(R.string.kaleyra_back)
        composeTestRule.onAllNodesWithContentDescription(enterFullscreen).assertCountEquals(2)
        composeTestRule.onAllNodesWithContentDescription(enterFullscreen).onFirst().performClick()
        composeTestRule.onAllNodesWithContentDescription(exitFullscreen).assertCountEquals(1)
        composeTestRule.onNodeWithContentDescription(back).performClick()
        composeTestRule.onAllNodesWithContentDescription(enterFullscreen).assertCountEquals(2)
    }

    // n of columns decision
    // quando mostri il callinfowidget, gli header degli stream si spostano
    // DONE quando sei in full screen, se premi il back esci dal fullscreen (sia per callinfowidget, che per lo stream header)
    // DONE se immagine e text del watermark sono null, nascondi il watermark
    // DONE se premi fullscreen su uno degli stream, entri in fullscreen
    // modifier header applicato solo alla prima riga
    // quando rimuovi uno stream in full screen, esce dalla mod full screen

    @Test
    fun test1() {
        showCallInfo = false
        val top1 = composeTestRule.onNodeWithText("user1").getBoundsInRoot().top
        showCallInfo = true
        val top2 = composeTestRule.onNodeWithText("user1").getBoundsInRoot().top
        assert(top1 < top2)
    }
}