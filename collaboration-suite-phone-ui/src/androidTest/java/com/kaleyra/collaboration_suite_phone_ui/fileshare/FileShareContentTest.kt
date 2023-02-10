package com.kaleyra.collaboration_suite_phone_ui.fileshare

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.model.SharedFileUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.model.mockDownloaSharedFile
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.view.FileShareContent
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.view.FileShareItemTag
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
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
    fun itemAvailableState_itemIsNotEnabled() {
        items = ImmutableList(listOf(mockDownloaSharedFile.copy(state = SharedFileUi.State.Available)))
        composeTestRule.onNodeWithTag(FileShareItemTag).assertIsNotEnabled()
    }

    @Test
    fun itemPendingState_itemIsNotEnabled() {
        items = ImmutableList(listOf(mockDownloaSharedFile.copy(state = SharedFileUi.State.Pending)))
        composeTestRule.onNodeWithTag(FileShareItemTag).assertIsNotEnabled()
    }

    @Test
    fun itemInProgressState_itemIsNotEnabled() {
        items = ImmutableList(
            listOf(
                mockDownloaSharedFile.copy(
                    state = SharedFileUi.State.InProgress(progress = .5f)
                )
            )
        )
        composeTestRule.onNodeWithTag(FileShareItemTag).assertIsNotEnabled()
    }

    @Test
    fun itemSuccessState_itemIsEnabled() {
        items =
            ImmutableList(listOf(mockDownloaSharedFile.copy(state = SharedFileUi.State.Success(uri = Uri.EMPTY))))
        composeTestRule.onNodeWithTag(FileShareItemTag).assertIsEnabled()
    }

    @Test
    fun itemErrorState_itemIsNotEnabled() {
        items = ImmutableList(listOf(mockDownloaSharedFile.copy(state = SharedFileUi.State.Error)))
        composeTestRule.onNodeWithTag(FileShareItemTag).assertIsNotEnabled()
    }

    @Test
    fun itemCancelledState_itemIsNotEnabled() {
        items = ImmutableList(listOf(mockDownloaSharedFile.copy(state = SharedFileUi.State.Cancelled)))
        composeTestRule.onNodeWithTag(FileShareItemTag).assertIsNotEnabled()
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