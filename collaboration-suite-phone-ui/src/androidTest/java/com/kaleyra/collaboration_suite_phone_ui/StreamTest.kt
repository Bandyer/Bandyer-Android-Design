package com.kaleyra.collaboration_suite_phone_ui

import android.net.Uri
import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.call.compose.ImmutableUri
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.Stream
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.StreamViewTestTag
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StreamTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var streamView by mutableStateOf<View?>(null)

    private var isAvatarVisible by mutableStateOf(false)

    @Before
    fun setUp() {
        composeTestRule.setContent {
            Stream(
                streamView = streamView,
                avatar = ImmutableUri(Uri.EMPTY),
                avatarVisible = isAvatarVisible
            )
        }
    }

    @Test
    fun streamViewNull_streamViewDoesNotExists() {
        streamView = null
        composeTestRule.onNodeWithTag(StreamViewTestTag).assertDoesNotExist()
    }

    @Test
    fun streamViewNotNull_streamViewIsDisplayed() {
        streamView = View(composeTestRule.activity)
        composeTestRule.onNodeWithTag(StreamViewTestTag).assertIsDisplayed()
    }

    @Test
    fun avatarVisibleTrue_avatarIsDisplayed() {
        isAvatarVisible = true
        composeTestRule.findAvatar().assertIsDisplayed()
    }

    @Test
    fun avatarVisibleFalse_avatarDoesNotExists() {
        isAvatarVisible = false
        composeTestRule.findAvatar().assertDoesNotExist()
    }
}