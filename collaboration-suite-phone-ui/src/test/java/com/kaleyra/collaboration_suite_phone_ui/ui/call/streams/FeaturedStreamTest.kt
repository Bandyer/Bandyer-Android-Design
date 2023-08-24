package com.kaleyra.collaboration_suite_phone_ui.ui.call.streams

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streamUiMock
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.FeaturedStream
import com.kaleyra.collaboration_suite_phone_ui.ui.findBackButton
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config


@RunWith(RobolectricTestRunner::class)
class FeaturedStreamTest: StreamParentComposableTest() {

    @get:Rule
    override val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var isBackPressed = false

    private var fullscreenClicked = false

    override var stream = mutableStateOf(streamUiMock)

    private var onBackPressed by mutableStateOf<(() -> Unit)?>(null)

    private var isFullscreen by mutableStateOf(false)

    @Before
    fun setUp() {
        composeTestRule.setContent {
            FeaturedStream(
                stream = stream.value,
                isFullscreen = isFullscreen,
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

    private fun findEnterFullscreenButton(): SemanticsNodeInteraction {
        val enterFullscreen = composeTestRule.activity.getString(R.string.kaleyra_enter_fullscreen)
        return composeTestRule.onNodeWithContentDescription(enterFullscreen)
    }

    private fun findExitFullscreenButton(): SemanticsNodeInteraction {
        val exitFullscreen = composeTestRule.activity.getString(R.string.kaleyra_exit_fullscreen)
        return composeTestRule.onNodeWithContentDescription(exitFullscreen)
    }
}