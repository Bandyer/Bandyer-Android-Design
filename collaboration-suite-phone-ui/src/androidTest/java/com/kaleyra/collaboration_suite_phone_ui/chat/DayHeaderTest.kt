package com.kaleyra.collaboration_suite_phone_ui.chat

import androidx.activity.ComponentActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.chat.conversation.view.item.DayHeaderItem
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant
import java.time.temporal.ChronoUnit

@RunWith(AndroidJUnit4::class)
class DayHeaderTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val time = MutableStateFlow<Long>(0)

    @Before
    fun setUp() {
        composeTestRule.setContent {
            DayHeaderItem(timestamp = time.collectAsState().value)
        }
    }

    @Test
    fun nowTime_todayIsDisplayed() {
        time.value = Instant.now().toEpochMilli()
        val today = composeTestRule.activity.getString(R.string.kaleyra_today)
        composeTestRule.onNodeWithText(today).assertIsDisplayed()
    }

    @Test
    fun yesterdayTime_yesterdayIsDisplayed() {
        time.value = Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli()
        val yesterday = composeTestRule.activity.getString(R.string.kaleyra_yesterday)
        composeTestRule.onNodeWithText(yesterday).assertIsDisplayed()
    }

}