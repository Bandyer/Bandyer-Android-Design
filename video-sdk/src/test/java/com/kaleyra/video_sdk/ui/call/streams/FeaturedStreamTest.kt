package com.kaleyra.video_sdk.ui.call.streams

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.stream.model.streamUiMock
import com.kaleyra.video_sdk.call.stream.view.featured.FeaturedStream
import com.kaleyra.video_sdk.ui.findBackButton
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner


@RunWith(RobolectricTestRunner::class)
class FeaturedStreamTest: StreamParentComposableTest() {

    @get:Rule
    override val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var isBackPressed = false

    private var fullscreenClicked = false

    override var stream = mutableStateOf(streamUiMock)

    private var onBackPressed by mutableStateOf<(() -> Unit)?>(null)

    private var isFullscreen by mutableStateOf(false)

    private var showFullscreen by mutableStateOf(true)

    @Before
    fun setUp() {
        composeTestRule.setContent {
            FeaturedStream(
                stream = stream.value,
                isFullscreen = isFullscreen,
                fullscreenVisible = showFullscreen,
                onBackPressed = onBackPressed,
                onFullscreenClick = { fullscreenClicked = true },
                isTesting = true
            )
        }
    }

    @After
    fun tearDown() {
        isBackPressed = false
        fullscreenClicked = false
        showFullscreen = true
        stream = mutableStateOf(streamUiMock)
        onBackPressed = null
        isFullscreen = false
    }

    @Test
    fun headerDisplaysUsername() {
        stream.value = streamUiMock.copy(username = "username")
        composeTestRule.onNodeWithText("username").assertIsDisplayed()
    }

    @Test
    fun onBackPressedNull_headerDoesNotHaveBackButton() {
        onBackPressed = null
        composeTestRule.findBackButton().assertDoesNotExist()
    }

    @Test
    fun onBackPressedNotNull_headerDisplaysBackButton() {
        onBackPressed = { }
        composeTestRule.findBackButton().assertIsDisplayed()
    }

    @Test
    fun userClicksBackButton_onBackPressedInvoked() {
        onBackPressed = { isBackPressed = true }
        composeTestRule.findBackButton().performClick()
        assert(isBackPressed)
    }

    @Test
    fun isFullscreenTrue_headerDisplaysExitFullscreenButton() {
        isFullscreen = true
        findEnterFullscreenButton().assertDoesNotExist()
        findExitFullscreenButton().assertIsDisplayed()
    }

    @Test
    fun isFullscreenFalse_headerDisplaysEnterFullscreenButton() {
        isFullscreen = false
        findEnterFullscreenButton().assertIsDisplayed()
        findExitFullscreenButton().assertDoesNotExist()
    }

    @Test
    fun userClicksFullscreenButton_onFullscreenClickInvoked() {
        isFullscreen = false
        findEnterFullscreenButton().performClick()
        assertEquals(true, fullscreenClicked)
    }

    @Test
    fun fullscreenVisibleFalse_fullscreenButtonNotExists() {
        showFullscreen = false
        findEnterFullscreenButton().assertDoesNotExist()
    }

    private fun findEnterFullscreenButton(): SemanticsNodeInteraction {
        val enterFullscreen = composeTestRule.activity.getString(R.string.kaleyra_enter_fullscreen)
        return composeTestRule.onNodeWithContentDescription(enterFullscreen)
    }

    private fun findExitFullscreenButton(): SemanticsNodeInteraction {
        val exitFullscreen = composeTestRule.activity.getString(R.string.kaleyra_exit_fullscreen)
        return composeTestRule.onNodeWithContentDescription(exitFullscreen)
    }
}