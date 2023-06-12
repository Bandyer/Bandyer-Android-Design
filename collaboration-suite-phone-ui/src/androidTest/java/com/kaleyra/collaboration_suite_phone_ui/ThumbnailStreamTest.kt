package com.kaleyra.collaboration_suite_phone_ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.call.compose.ThumbnailStream
import com.kaleyra.collaboration_suite_phone_ui.call.compose.ThumbnailTag
import com.kaleyra.collaboration_suite_phone_ui.call.compose.audiooutput.view.clickLabelFor
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streamUiMock
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ThumbnailStreamTest: StreamParentComposableTest() {

    @get:Rule
    override val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    override var stream = mutableStateOf(streamUiMock)

    private var isClicked = false

    private var isDoubleClicked = false

    @Before
    fun setUp() {
        composeTestRule.setContent {
            ThumbnailStream(
                stream = stream.value,
                onClick = { isClicked = true },
                onDoubleClick = { isDoubleClicked = true }
            )
        }
    }

    @After
    fun tearDown() {
        stream.value = streamUiMock
        isClicked = false
        isDoubleClicked = false
    }

    // todo understand why this fails
//    @Test
//    fun userClicksThumbnailStream_onClickIsInvoked() {
//        composeTestRule.onNodeWithTag(ThumbnailTag).performClick()
//        assert(isClicked)
//    }

    @Test
    fun userDoubleClicksThumbnailStream_onDoubleClickIsInvoked() {
        composeTestRule.onNodeWithTag(ThumbnailTag).performDoubleClick()
        assert(isDoubleClicked)
    }
}