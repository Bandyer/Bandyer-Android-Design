package com.kaleyra.collaboration_suite_phone_ui

import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.StreamTile
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.StreamHeaderTestTag
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.StreamTestTag
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StreamTileTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var isBackPressed = false

    private var isFullscreenClicked = false

    private var view by mutableStateOf<View?>(null)

    private var onBackPressed by mutableStateOf<(() -> Unit)?>(null)

    private var isFullscreen by mutableStateOf(false)

    private var showAvatar by mutableStateOf(false)

    private var showHeader  by mutableStateOf(false)

    @Before
    fun setUp() {
        composeTestRule.setContent {
            StreamTile(
                view = view,
                username = "username",
                onBackPressed = onBackPressed,
                onFullscreenClick = { isFullscreenClicked = true },
                showAvatar = showAvatar,
                showHeader = showHeader,
                isFullscreen = isFullscreen
            )
        }
    }

    @Test
    fun viewNotNull_streamViewIsDisplayed() {
        view = View(composeTestRule.activity)
        findStreamView().assertIsDisplayed()
    }

    @Test
    fun viewNull_streamViewDoesNotExists() {
        view = null
        findStreamView().assertDoesNotExist()
    }

    @Test
    fun headerDisplaysUsername() {
        showHeader = true
        composeTestRule.onNodeWithText("username").assertIsDisplayed()
    }

    @Test
    fun showHeaderTrue_headerIsDisplayed() {
        showHeader = true
        composeTestRule.onNodeWithTag(StreamHeaderTestTag).assertIsDisplayed()
    }

    @Test
    fun showHeaderFalse_headerDoesNotExists() {
        showHeader = false
        composeTestRule.onNodeWithTag(StreamHeaderTestTag).assertDoesNotExist()
    }

    @Test
    fun showAvatarTrue_avatarIsDisplayed() {
        showAvatar = true
        findAvatar().assertIsDisplayed()
    }

    @Test
    fun showAvatarFalse_avatarDoesNotExists() {
        showAvatar = false
        findAvatar().assertDoesNotExist()
    }

    @Test
    fun onBackPressedNull_headerDoesNotHaveBackButton() {
        showHeader = true
        onBackPressed = null
        findBackButton().assertDoesNotExist()
    }

    @Test
    fun onBackPressedNotNull_headerDisplaysBackButton() {
        showHeader = true
        onBackPressed = { }
        findBackButton().assertIsDisplayed()
    }

    @Test
    fun userClicksBackButton_onBackPressedInvoked() {
        showHeader = true
        onBackPressed = { isBackPressed = true }
        findBackButton().performClick()
        assert(isBackPressed)
    }

    @Test
    fun isFullscreenTrue_headerDisplaysExitFullscreenButton() {
        showHeader = true
        isFullscreen = true
        findEnterFullscreenButton().assertDoesNotExist()
        findExitFullscreenButton().assertIsDisplayed()
    }

    @Test
    fun isFullscreenFalse_headerDisplaysEnterFullscreenButton() {
        showHeader = true
        isFullscreen = false
        findEnterFullscreenButton().assertIsDisplayed()
        findExitFullscreenButton().assertDoesNotExist()
    }

    @Test
    fun userClicksFullscreenButton_onFullscreenClickInvoked() {
        showHeader = true
        isFullscreen = false
        findEnterFullscreenButton().performClick()
        assert(isFullscreenClicked)
    }

    private fun findStreamView(): SemanticsNodeInteraction {
        return composeTestRule.onNodeWithTag(StreamTestTag)
    }

    private fun findBackButton(): SemanticsNodeInteraction {
        val back = composeTestRule.activity.getString(R.string.kaleyra_back)
        return composeTestRule.onNodeWithContentDescription(back)
    }

    private fun findAvatar(): SemanticsNodeInteraction {
        val avatar = composeTestRule.activity.getString(R.string.kaleyra_avatar)
        return composeTestRule.onNodeWithContentDescription(avatar)
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