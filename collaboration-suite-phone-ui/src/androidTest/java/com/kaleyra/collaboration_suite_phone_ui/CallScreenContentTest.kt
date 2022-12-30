package com.kaleyra.collaboration_suite_phone_ui

import android.content.res.Configuration
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.call.compose.*
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.CallInfoUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.callInfoMock
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CallScreenContentTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var state by mutableStateOf(defaultState())

    private var isBackPressed = false

    @Before
    fun setUp() {
        state = defaultState()
        composeTestRule.setContent {
            CallScreenContent(
                state = state,
                onBackPressed = { isBackPressed = true }
            )
        }
        isBackPressed = false
    }

    @Test
    fun hideCallInfo_callInfoWidgetDoesNotExists() {
        state = defaultState(showCallInfo = true)
        composeTestRule.onNodeWithText("subtitle").assertIsDisplayed()
        state.hideCallInfo()
        composeTestRule.onNodeWithText("subtitle").assertDoesNotExist()
    }

    @Test
    fun hideCallInfo_showCallInfoIsFalse() {
        state = defaultState(showCallInfo = true)
        state.hideCallInfo()
        assert(!state.showCallInfo)
    }

    @Test
    fun showCallInfo_callInfoWidgetIsDisplayed() {
        composeTestRule.onNodeWithText("subtitle").assertDoesNotExist()
        state.showCallInfo()
        composeTestRule.onNodeWithText("subtitle").assertIsDisplayed()
    }

    @Test
    fun showCallInfo_showCallInfoIsTrue() {
        state = defaultState(showCallInfo = false)
        state.showCallInfo()
        assert(state.showCallInfo)
    }

    @Test
    fun userClicksCallInfoWidgetBackButton_onBackPressedInvoked() {
        state.showCallInfo()
        composeTestRule.onNodeWithContentDescription(getBackText()).performClick()
        assert(isBackPressed)
    }

    @Test
    fun userClicksStreamBackButton_onBackPressedInvoked() {
        state = defaultState(showCallInfo = false)
        composeTestRule.onNodeWithContentDescription(getBackText()).performClick()
        assert(isBackPressed)
    }

    @Test
    fun watermarkIsNull_callInfoWidgetIsDisplayedRightToBackButton() {
        state = defaultState(
            showCallInfo = true,
            callInfo = callInfoMock.copy(watermarkInfo = null, subtitle = "subtitle")
        )
        val subtitleBounds = composeTestRule.onNodeWithText("subtitle").getBoundsInRoot()
        val backBounds = composeTestRule.onNodeWithContentDescription(getBackText()).getBoundsInRoot()
        assert(subtitleBounds.left > backBounds.right)
    }

    @Test
    fun userClicksEnterFullscreen_fullscreenStreamIsDisplayed() {
        composeTestRule.onAllNodesWithContentDescription(getEnterFullscreenText()).onFirst().performClick()
        composeTestRule.fullscreenStreamIsDisplayed("user1")
    }

    @Test
    fun userClicksExitFullscreen_streamsGridIsDisplayed() {
        state = defaultState(fullscreenStream = streamUiMock.copy(username = "user1"))
        composeTestRule.onNodeWithContentDescription(getExitFullscreenText()).performClick()
        composeTestRule.streamGridIsDisplayed("user1", "user2")
    }

    @Test
    fun fullscreenStream_userClicksCallInfoWidgetBackButton_streamsGridIsDisplayed() {
        state = defaultState(showCallInfo = true, fullscreenStream = streamUiMock.copy(username = "user1"))
        composeTestRule.onAllNodesWithContentDescription(getExitFullscreenText()).assertCountEquals(1)
        composeTestRule.onNodeWithContentDescription(getBackText()).performClick()
        composeTestRule.streamGridIsDisplayed("user1", "user2")
    }

    @Test
    fun fullscreenStream_userClicksStreamBackButton_streamsGridIsDisplayed() {
        state = defaultState(showCallInfo = false, fullscreenStream = streamUiMock.copy(username = "user1"))
        composeTestRule.onAllNodesWithContentDescription(getExitFullscreenText()).assertCountEquals(1)
        composeTestRule.onNodeWithContentDescription(getBackText()).performClick()
        composeTestRule.streamGridIsDisplayed("user1", "user2")
    }

    @Test
    fun enterFullscreenMode_fullscreenStreamIsDisplayed() {
        composeTestRule.streamGridIsDisplayed("user1", "user2")
        state.enterFullscreenMode(streamUiMock.copy(username = "user1"))
        composeTestRule.fullscreenStreamIsDisplayed("user1")
    }

    @Test
    fun exitFullscreenMode_streamsGridIsDisplayed() {
        state = defaultState(fullscreenStream = streamUiMock.copy(username = "user1"))
        composeTestRule.onNodeWithText("user1").assertIsDisplayed()
        composeTestRule.onAllNodesWithContentDescription(getExitFullscreenText()).assertCountEquals(1)
        state.exitFullscreenMode()
        composeTestRule.streamGridIsDisplayed("user1", "user2")
    }

    @Test
    fun enterFullscreenMode_stateFullscreenStreamIsTheCorrect() {
        val stream = streamUiMock.copy(username = "user1")
        assertEquals(null, state.fullscreenStream)
        state.enterFullscreenMode(stream)
        assertEquals(stream, state.fullscreenStream)
    }

    @Test
    fun exitFullscreenMode_stateFullscreenStreamIsNull() {
        val stream = streamUiMock.copy(username = "user1")
        state = defaultState(fullscreenStream = stream)
        assertEquals(stream, state.fullscreenStream, )
        state.exitFullscreenMode()
        assertEquals(null, state.fullscreenStream)
    }

    @Test
    fun deviceIsInPortraitAndMaxWidthIsLessThan600Dp_oneColumn() {
       state = defaultState(
           configuration = mockk { orientation = Configuration.ORIENTATION_PORTRAIT },
           maxWidth = 599.dp
       )
        assertEquals(1, state.columns)
    }

    @Test
    fun deviceIsInPortraitAndMaxWidthIsMoreThan600DpAndStreamsCountIsGreaterThanTwo_twoColumns() {
        state = defaultState(
            configuration = mockk { orientation = Configuration.ORIENTATION_PORTRAIT },
            maxWidth = 600.dp
        )
        assertEquals(2, state.columns)
    }

    @Test
    fun deviceIsNotInPortraitAndStreamsCountIsGreaterThanOne_twoColumns() {
        state = defaultState(
            configuration = mockk { orientation = Configuration.ORIENTATION_LANDSCAPE },
            streams = ImmutableList(
                listOf(
                    streamUiMock.copy(username = "user1"),
                    streamUiMock.copy(username = "user2"),
                    streamUiMock.copy(username = "user3")
                )
            )
        )
        assertEquals(2, state.columns)
    }

    @Test
    fun deviceIsInPortraitAndMaxWidthIsLessThan600Dp_columnsNumberIsOne() {
        state = defaultState(
            configuration = mockk { orientation = Configuration.ORIENTATION_PORTRAIT },
            maxWidth = 599.dp
        )
        assertEquals(1, state.columns)
    }

    @Test
    fun gridHasOneColumnAndCallInfoWidgetIsDisplayed_firstStreamHeaderIsShifted() {
        state = defaultState(
            configuration = mockk { orientation = Configuration.ORIENTATION_PORTRAIT },
            maxWidth = 400.dp,
            showCallInfo = false
        )
        val streamOneTextTop = composeTestRule.onNodeWithText("user1", useUnmergedTree = true).getBoundsInRoot().top
        val streamTwoTextTop = composeTestRule.onNodeWithText("user2", useUnmergedTree = true).getBoundsInRoot().top
        state.showCallInfo()
        val newStreamOneTextTop = composeTestRule.onNodeWithText("user1", useUnmergedTree = true).getBoundsInRoot().top
        val newStreamTwoTextTop = composeTestRule.onNodeWithText("user2", useUnmergedTree = true).getBoundsInRoot().top
        val callInfoWidgetHeight = composeTestRule.onNodeWithTag(CallInfoWidgetTag).getBoundsInRoot().height
        assertEquals(newStreamOneTextTop, streamOneTextTop + callInfoWidgetHeight)
        assertEquals(streamTwoTextTop, newStreamTwoTextTop)
    }

    @Test
    fun gridHasTwoColumnsAndCallInfoWidgetIsDisplayed_bothStreamsHeaderAreShifted() {
        state = defaultState(
            configuration = mockk { orientation = Configuration.ORIENTATION_PORTRAIT },
            maxWidth = 800.dp,
            showCallInfo = false
        )
        val streamOneTextTop = composeTestRule.onNodeWithText("user1", useUnmergedTree = true).getBoundsInRoot().top
        val streamTwoTextTop = composeTestRule.onNodeWithText("user2", useUnmergedTree = true).getBoundsInRoot().top
        state.showCallInfo()
        val newStreamOneTextTop = composeTestRule.onNodeWithText("user1", useUnmergedTree = true).getBoundsInRoot().top
        val newStreamTwoTextTop = composeTestRule.onNodeWithText("user2", useUnmergedTree = true).getBoundsInRoot().top
        val callInfoWidgetHeight = composeTestRule.onNodeWithTag(CallInfoWidgetTag).getBoundsInRoot().height
        assertEquals(newStreamOneTextTop, streamOneTextTop + callInfoWidgetHeight)
        assertEquals(newStreamTwoTextTop, streamTwoTextTop + callInfoWidgetHeight)
    }

    @Test
    fun fullscreenStreamAndCallInfoWidgetIsDisplayed_streamHeaderIsShifted() {
        state = defaultState(
            fullscreenStream = streamUiMock.copy(username = "user1"),
            showCallInfo = false
        )
        val streamOneTextTop = composeTestRule.onNodeWithText("user1", useUnmergedTree = true).getBoundsInRoot().top
        state.showCallInfo()
        val newStreamOneTextTop = composeTestRule.onNodeWithText("user1", useUnmergedTree = true).getBoundsInRoot().top
        val callInfoWidgetHeight = composeTestRule.onNodeWithTag(CallInfoWidgetTag).getBoundsInRoot().height
        assertEquals(newStreamOneTextTop, streamOneTextTop + callInfoWidgetHeight)
    }

    @Test
    fun fullscreenStreamIsRemovedFromStreamsList_streamsGridIsDisplayed() {
        val stream = streamUiMock.copy(username = "user1")
        state = defaultState(
            streams = ImmutableList(listOf(stream, streamUiMock.copy(username = "user2"))),
            fullscreenStream = stream
        )
        composeTestRule.fullscreenStreamIsDisplayed("user1")
        state = defaultState(
            streams = ImmutableList(listOf(streamUiMock.copy(username = "user2"), streamUiMock.copy(username = "user3"))),
            fullscreenStream = stream
        )
        composeTestRule.streamGridIsDisplayed("user2", "user3")
    }

    private fun ComposeContentTestRule.streamGridIsDisplayed(vararg usernames: String) {
        onAllNodesWithContentDescription(getEnterFullscreenText()).assertCountEquals(usernames.size)
        usernames.forEach { onNodeWithText(it).assertIsDisplayed() }
    }

    private fun ComposeContentTestRule.fullscreenStreamIsDisplayed(username: String) {
        val exitFullscreen = getExitFullscreenText()
        onNodeWithText(username).assertIsDisplayed()
        onAllNodesWithContentDescription(exitFullscreen).assertCountEquals(1)
        onNodeWithContentDescription(exitFullscreen).assertIsDisplayed()
    }

    private fun getBackText() = composeTestRule.activity.getString(R.string.kaleyra_back)

    private fun getEnterFullscreenText() = composeTestRule.activity.getString(R.string.kaleyra_enter_fullscreen)

    private fun getExitFullscreenText() = composeTestRule.activity.getString(R.string.kaleyra_exit_fullscreen)

    private fun defaultState(
        streams: ImmutableList<StreamUi> = ImmutableList(
            listOf(
                streamUiMock.copy(username = "user1"),
                streamUiMock.copy(username = "user2")
            )
        ),
        callInfo: CallInfoUi = callInfoMock.copy(subtitle = "subtitle"),
        configuration: Configuration = mockk(),
        maxWidth: Dp = 400.dp,
        showCallInfo: Boolean = false,
        fullscreenStream: StreamUi? = null
    ): CallScreenContentState {
        return CallScreenContentState(
            streams = streams,
            callInfo = callInfo,
            configuration = configuration,
            maxWidth = maxWidth,
            showCallInfo = showCallInfo,
            fullscreenStream = fullscreenStream
        )
    }
}