package com.kaleyra.collaboration_suite_phone_ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.call.compose.DialingContent
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streamUiMock
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.callInfoMock
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DialingContentTest: PreCallContentTest() {

    @get:Rule
    override val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    override var stream = mutableStateOf(streamUiMock)

    override var callInfo = mutableStateOf(callInfoMock)

    @Before
    fun setUp() {
        composeTestRule.setContent {
            DialingContent(
                stream = stream.value,
                callInfo = callInfo.value
            )
        }
    }
}