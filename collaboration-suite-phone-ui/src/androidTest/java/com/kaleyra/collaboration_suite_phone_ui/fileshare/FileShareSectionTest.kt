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
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.FileShareSection
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.model.FileShareUiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.model.TransferUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.model.mockDownloadTransfer
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.model.mockUploadTransfer
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.view.FileShareItemTag
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FileShareSectionTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var items by mutableStateOf(ImmutableList(emptyList<TransferUi>()))

    private var isFabClicked = false

    private var actualTransfer: TransferUi? = null

    @Before
    fun setUp() {
        composeTestRule.setContent {
            FileShareSection(
                uiState = FileShareUiState(transferList = items),
                onFabClick = { isFabClicked = true },
                onItemClick = { actualTransfer = it },
                onItemActionClick = { actualTransfer = it })
        }
        actualTransfer = null
    }

    @Test
    fun emptyItems_noItemsUIDisplayed() {
        val title = composeTestRule.activity.getString(R.string.kaleyra_no_file_shared)
        val subtitle = composeTestRule.activity.getString(R.string.kaleyra_click_to_share_file)
        items = ImmutableList(listOf(mockUploadTransfer))
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
        items = ImmutableList(listOf(mockDownloadTransfer))
        val iconDescription = composeTestRule.activity.getString(R.string.kaleyra_fileshare_add_description)
        val text = composeTestRule.activity.getString(R.string.kaleyra_fileshare_add).uppercase()
        composeTestRule.onNodeWithContentDescription(iconDescription).assertIsDisplayed()
        composeTestRule.onNodeWithText(text).assertDoesNotExist()
    }

    @Test
    fun oneTransferItem_itemDisplayed() {
        items = ImmutableList(listOf(mockDownloadTransfer))
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
        val transfer = mockDownloadTransfer.copy(state = TransferUi.State.Success(uri = Uri.EMPTY))
        items = ImmutableList(listOf(transfer))
        composeTestRule.onNodeWithTag(FileShareItemTag).performClick()
        assertEquals(transfer, actualTransfer)
    }

    @Test
    fun userClicksItemAction_onItemActionClickInvoked() {
        val transfer = mockDownloadTransfer.copy(state = TransferUi.State.Pending)
        items = ImmutableList(listOf(transfer))
        composeTestRule
            .onNodeWithTag(FileShareItemTag)
            .onChildren()
            .filterToOne(hasClickAction())
            .performClick()
        assertEquals(transfer, actualTransfer)
    }
}