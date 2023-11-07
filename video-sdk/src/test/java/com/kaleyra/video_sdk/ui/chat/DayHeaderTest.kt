package com.kaleyra.video_sdk.ui.chat

import androidx.activity.ComponentActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.chat.conversation.view.item.DayHeaderItem
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Instant
import java.time.temporal.ChronoUnit


@RunWith(RobolectricTestRunner::class)
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