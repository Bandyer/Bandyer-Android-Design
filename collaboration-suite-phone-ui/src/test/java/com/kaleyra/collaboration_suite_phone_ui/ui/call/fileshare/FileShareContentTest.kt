package com.kaleyra.collaboration_suite_phone_ui.ui.call.fileshare

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.kaleyra.collaboration_suite_phone_ui.call.compose.ImmutableUri
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.model.SharedFileUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.model.mockDownloadSharedFile
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.view.FileShareContent
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.view.FileShareItemDividerTag
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.view.FileShareItemTag
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class FileShareContentTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var items by mutableStateOf(ImmutableList(emptyList<SharedFileUi>()))

    private var actualSharedFile: SharedFileUi? = null

    @Before
    fun setUp() {
        composeTestRule.setContent {
            FileShareContent(
                items = items,
                onItemClick = { actualSharedFile = it },
                onItemActionClick = { actualSharedFile = it }
            )
        }
    }

    @After
    fun tearDown() {
        items = ImmutableList(emptyList())
        actualSharedFile = null
    }

    @Test
    fun itemDividerCountIsNumberOfItemsMinusOne() {
        items = ImmutableList(listOf(mockDownloadSharedFile.copy(id = "1"), mockDownloadSharedFile.copy(id = "2"), mockDownloadSharedFile.copy(id = "3")))
        composeTestRule.onAllNodesWithTag(FileShareItemDividerTag).assertCountEquals(2)
    }

    @Test
    fun itemAvailableState_itemIsNotEnabled() {
        items = ImmutableList(listOf(mockDownloadSharedFile.copy(state = SharedFileUi.State.Available)))
        composeTestRule.onNodeWithTag(FileShareItemTag).assertIsNotEnabled()
    }

    @Test
    fun itemPendingState_itemIsNotEnabled() {
        items = ImmutableList(listOf(mockDownloadSharedFile.copy(state = SharedFileUi.State.Pending)))
        composeTestRule.onNodeWithTag(FileShareItemTag).assertIsNotEnabled()
    }

    @Test
    fun itemInProgressState_itemIsNotEnabled() {
        items = ImmutableList(
            listOf(
                mockDownloadSharedFile.copy(
                    state = SharedFileUi.State.InProgress(progress = .5f)
                )
            )
        )
        composeTestRule.onNodeWithTag(FileShareItemTag).assertIsNotEnabled()
    }

    @Test
    fun itemSuccessState_itemIsEnabled() {
        items =
            ImmutableList(listOf(mockDownloadSharedFile.copy(state = SharedFileUi.State.Success(uri = ImmutableUri(Uri.EMPTY)))))
        composeTestRule.onNodeWithTag(FileShareItemTag).assertIsEnabled()
    }

    @Test
    fun itemErrorState_itemIsNotEnabled() {
        items = ImmutableList(listOf(mockDownloadSharedFile.copy(state = SharedFileUi.State.Error)))
        composeTestRule.onNodeWithTag(FileShareItemTag).assertIsNotEnabled()
    }

    @Test
    fun itemCancelledState_itemIsNotEnabled() {
        items = ImmutableList(listOf(mockDownloadSharedFile.copy(state = SharedFileUi.State.Cancelled)))
        composeTestRule.onNodeWithTag(FileShareItemTag).assertIsNotEnabled()
    }

    @Test
    fun userClicksSuccessStateItem_onItemClickInvoked() {
        val sharedFile = mockDownloadSharedFile.copy(state = SharedFileUi.State.Success(uri = ImmutableUri(Uri.EMPTY)))
        items = ImmutableList(listOf(sharedFile))
        composeTestRule.onNodeWithTag(FileShareItemTag).performClick()
        assertEquals(sharedFile, actualSharedFile)
    }

    @Test
    fun userClicksItemAction_onItemActionClickInvoked() {
        val sharedFile = mockDownloadSharedFile.copy(state = SharedFileUi.State.Pending)
        items = ImmutableList(listOf(sharedFile))
        composeTestRule
            .onNodeWithTag(FileShareItemTag)
            .onChildren()
            .filterToOne(hasClickAction())
            .performClick()
        assertEquals(sharedFile, actualSharedFile)
    }
}