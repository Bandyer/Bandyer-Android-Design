package com.kaleyra.collaboration_suite_phone_ui.fileshare

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.FileShareComponent
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.model.FileShareUiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.model.SharedFileUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.model.mockDownloaSharedFile
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.model.mockUploadSharedFile
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.view.FileShareItemTag
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FileShareComponentTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var items by mutableStateOf(ImmutableList(emptyList<SharedFileUi>()))

    private var isFabClicked = false

    private var actualSharedFile: SharedFileUi? = null

    @Before
    fun setUp() {
        composeTestRule.setContent {
            FileShareComponent(
                uiState = FileShareUiState(sharedFiles = items),
                onFabClick = { isFabClicked = true },
                onItemClick = { actualSharedFile = it },
                onItemActionClick = { actualSharedFile = it })
        }
    }

    @After
    fun tearDown() {
        items = ImmutableList(emptyList())
        isFabClicked = false
        actualSharedFile = null
    }

    @Test
    fun emptyItems_noItemsUIDisplayed() {
        val title = composeTestRule.activity.getString(R.string.kaleyra_no_file_shared)
        val subtitle = composeTestRule.activity.getString(R.string.kaleyra_click_to_share_file)
        items = ImmutableList(listOf(mockUploadSharedFile))
        composeTestRule.onNodeWithText(title).assertDoesNotExist()
        composeTestRule.onNodeWithText(subtitle).assertDoesNotExist()
        items = ImmutableList(listOf())
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
        composeTestRule.onNodeWithText(subtitle).assertIsDisplayed()
    }

    @Test
    fun emptyItems_fabTextDisplayed() {
        val iconDescription = composeTestRule.activity.getString(R.string.kaleyra_fileshare_add_description)
        val text = composeTestRule.activity.getString(R.string.kaleyra_fileshare_add).uppercase()
        composeTestRule.onNodeWithContentDescription(iconDescription).assertIsDisplayed()
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun atLeastOneItem_fabTextNotExist() {
        items = ImmutableList(listOf(mockDownloaSharedFile))
        val iconDescription = composeTestRule.activity.getString(R.string.kaleyra_fileshare_add_description)
        val text = composeTestRule.activity.getString(R.string.kaleyra_fileshare_add).uppercase()
        composeTestRule.onNodeWithContentDescription(iconDescription).assertIsDisplayed()
        composeTestRule.onNodeWithText(text).assertDoesNotExist()
    }

    @Test
    fun oneSharedFileItem_itemDisplayed() {
        items = ImmutableList(listOf(mockDownloaSharedFile))
        composeTestRule.onNodeWithTag(FileShareItemTag).assertIsDisplayed()
    }

    @Test
    fun userClicksFab_onFabClickInvoked() {
        val add = composeTestRule.activity.getString(R.string.kaleyra_fileshare_add_description)
        composeTestRule.onNodeWithContentDescription(add).performClick()
        assert(isFabClicked)
    }

    @Test
    fun userClicksSuccessStateItem_onItemClickInvoked() {
        val sharedFile = mockDownloaSharedFile.copy(state = SharedFileUi.State.Success(uri = Uri.EMPTY))
        items = ImmutableList(listOf(sharedFile))
        composeTestRule.onNodeWithTag(FileShareItemTag).performClick()
        assertEquals(sharedFile, actualSharedFile)
    }

    @Test
    fun userClicksItemAction_onItemActionClickInvoked() {
        val sharedFile = mockDownloaSharedFile.copy(state = SharedFileUi.State.Pending)
        items = ImmutableList(listOf(sharedFile))
        composeTestRule
            .onNodeWithTag(FileShareItemTag)
            .onChildren()
            .filterToOne(hasClickAction())
            .performClick()
        assertEquals(sharedFile, actualSharedFile)
    }
}