package com.kaleyra.collaboration_suite_phone_ui.ui.call.virtualbackground

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.virtualbackground.VirtualBackgroundComponent
import com.kaleyra.collaboration_suite_phone_ui.call.virtualbackground.model.VirtualBackgroundUi
import com.kaleyra.collaboration_suite_phone_ui.call.virtualbackground.model.VirtualBackgroundUiState
import com.kaleyra.collaboration_suite_phone_ui.common.immutablecollections.ImmutableList
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner


@RunWith(RobolectricTestRunner::class)
class VirtualBackgroundComponentTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var items by mutableStateOf(ImmutableList(listOf<VirtualBackgroundUi>()))

    private var backgroundClicked: VirtualBackgroundUi? = null

    private var isCloseClicked = false

    @Before
    fun setUp() {
        composeTestRule.setContent {
            VirtualBackgroundComponent(
                uiState = VirtualBackgroundUiState(backgroundList = items),
                onItemClick = { backgroundClicked = it },
                onCloseClick = { isCloseClicked = true }
            )
        }
    }

    @After
    fun tearDown() {
        items = ImmutableList(listOf())
        backgroundClicked = null
        isCloseClicked = false
    }

    @Test
    fun virtualBackgroundTitleDisplayed() {
        val title = composeTestRule.activity.getString(R.string.kaleyra_virtual_background_picker_title)
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
    }

    @Test
    fun userClicksClose_onCloseClickInvoked() {
        val close = composeTestRule.activity.getString(R.string.kaleyra_close)
        composeTestRule.onNodeWithContentDescription(close).performClick()
        assert(isCloseClicked)
    }

    @Test
    fun userClicksOnItem_onItemClickInvoked() {
        items = ImmutableList(listOf(VirtualBackgroundUi.None, VirtualBackgroundUi.Blur("id")))
        val blur = composeTestRule.activity.getString(R.string.kaleyra_virtual_background_blur)
        composeTestRule.onNodeWithText(blur).performClick()
        Assert.assertEquals(VirtualBackgroundUi.Blur("id"), backgroundClicked)
    }
}