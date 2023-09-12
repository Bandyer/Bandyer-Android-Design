package com.kaleyra.collaboration_suite_phone_ui.call.streams

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
import com.kaleyra.collaboration_suite_phone_ui.common.avatar.model.ImmutableUri
import com.kaleyra.collaboration_suite_phone_ui.call.ImmutableView
import com.kaleyra.collaboration_suite_phone_ui.findAvatar
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StreamTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var isAvatarVisible by mutableStateOf(false)

    @Before
    fun setUp() {
        composeTestRule.setContent {
            Stream(
                streamView = ImmutableView(View(composeTestRule.activity)),
                avatar = ImmutableUri(Uri.EMPTY),
                avatarVisible = isAvatarVisible
            )
        }
    }

    @After
    fun tearDown() {
        isAvatarVisible = false
    }

    @Test
    fun streamViewIsDisplayed() {
        composeTestRule.onNodeWithTag(StreamViewTestTag).assertIsDisplayed()
    }

    @Test
    fun avatarVisibleFalse_avatarIsNotDisplayed() {
        isAvatarVisible = false
        composeTestRule.findAvatar().assertDoesNotExist()
    }

    @Test
    fun avatarVisibleTrue_avatarIsDisplayed() {
        isAvatarVisible = true
        composeTestRule.findAvatar().assertIsDisplayed()
    }

}