package com.kaleyra.collaboration_suite_phone_ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streamUiMock
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.FeaturedStream
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FeaturedStreamTest: StreamParentComposableTest() {

    @get:Rule
    override val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var isBackPressed = false

    private var isFullscreenClicked = false

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
                onFullscreenClick = { isFullscreenClicked = true }
            )
        }
    }

    @Test
    fun headerDisplaysUsername() {
        stream.value = streamUiMock.copy(username = "username")
        composeTestRule.onNodeWithText("username").assertIsDisplayed()
    }

    @Test
    fun onBackPressedNull_headerDoesNotHaveBackButton() {
        onBackPressed = null
        findBackButton().assertDoesNotExist()
    }

    @Test
    fun onBackPressedNotNull_headerDisplaysBackButton() {
        onBackPressed = { }
        findBackButton().assertIsDisplayed()
    }

    @Test
    fun userClicksBackButton_onBackPressedInvoked() {
        onBackPressed = { isBackPressed = true }
        findBackButton().performClick()
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
        assert(isFullscreenClicked)
    }

    private fun findBackButton(): SemanticsNodeInteraction {
        val back = composeTestRule.activity.getString(R.string.kaleyra_back)
        return composeTestRule.onNodeWithContentDescription(back)
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