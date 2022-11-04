package com.kaleyra.collaboration_suite_phone_ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.call.compose.AudioDevice
import com.kaleyra.collaboration_suite_phone_ui.call.compose.AudioRoute
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AudioRouteTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private var items by mutableStateOf(ImmutableList(listOf<AudioDevice>()))

    private var onItemClicked = false

    @Before
    fun setUp() {
        composeTestRule.setContent {
            AudioRoute(items = items, onItemClick = { onItemClicked = true })
        }
    }
}