package com.kaleyra.collaboration_suite_phone_ui.fileshare

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ContextExtensions
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ContextExtensions.tryToOpenFile
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.compose.FilePickActivity
import com.kaleyra.collaboration_suite_phone_ui.call.compose.ImmutableUri
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.FileShareComponent
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.model.FileShareUiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.model.SharedFileUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.model.mockDownloadSharedFile
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.model.mockUploadSharedFile
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.view.FileShareItemTag
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import io.mockk.*
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

    private var showUnableToOpenFileSnackBar by mutableStateOf(false)

    private var showCancelledFileSnackBar by mutableStateOf(false)

    private var showFileSizeLimitAlertDialog by mutableStateOf(false)

    private var uploadUri: Uri? = null

    private var downloadId: String? = null

    private var cancelId: String? = null

    @Before
    fun setUp() {
        composeTestRule.setContent {
            FileShareComponent(
                uiState = FileShareUiState(sharedFiles = items),
                showUnableToOpenFileSnackBar = showUnableToOpenFileSnackBar,
                showCancelledFileSnackBar = showCancelledFileSnackBar,
                showFileSizeLimitAlertDialog = showFileSizeLimitAlertDialog,
                onUpload = { uploadUri = it },
                onDownload = { downloadId = it },
                onShareCancel = { cancelId = it },
                snackBarHostState = SnackbarHostState()
            )
        }
    }

    @After
    fun tearDown() {
        items = ImmutableList(emptyList())
        showUnableToOpenFileSnackBar = false
        showCancelledFileSnackBar = false
        showFileSizeLimitAlertDialog = false
        uploadUri = null
        downloadId = null
        cancelId = null
        unmockkAll()
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
        items = ImmutableList(listOf(mockDownloadSharedFile))
        val iconDescription = composeTestRule.activity.getString(R.string.kaleyra_fileshare_add_description)
        val text = composeTestRule.activity.getString(R.string.kaleyra_fileshare_add).uppercase()
        composeTestRule.onNodeWithContentDescription(iconDescription).assertIsDisplayed()
        composeTestRule.onNodeWithText(text).assertDoesNotExist()
    }

    @Test
    fun oneSharedFileItem_itemDisplayed() {
        items = ImmutableList(listOf(mockDownloadSharedFile))
        composeTestRule.onNodeWithTag(FileShareItemTag).assertIsDisplayed()
    }

    @Test
    fun showUnableToOpenFileSnackBarTrue_snackBarIsDisplayed() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_fileshare_impossible_open_file)
        showUnableToOpenFileSnackBar = false
        composeTestRule.onNodeWithText(text).assertDoesNotExist()
        showUnableToOpenFileSnackBar = true
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun showCancelledFileSnackBarTrue_snackBarIsDisplayed() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_fileshare_file_cancelled)
        showCancelledFileSnackBar = false
        composeTestRule.onNodeWithText(text).assertDoesNotExist()
        showCancelledFileSnackBar = true
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun showFileSizeLimitAlertDialogTrue_dialogIsDisplayed() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_max_bytes_dialog_title)
        showFileSizeLimitAlertDialog = false
        composeTestRule.onNodeWithText(text).assertDoesNotExist()
        showFileSizeLimitAlertDialog = true
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun userClicksFab_filePickActivityLaunched() {
        Intents.init()
        val add = composeTestRule.activity.getString(R.string.kaleyra_fileshare_add_description)
        composeTestRule.onNodeWithContentDescription(add).performClick()
        intended(hasComponent(FilePickActivity::class.java.name))
        Intents.release()
    }

    @Test
    fun userClicksSuccessStateItem_tryToOpenFileInvoked() {
        mockkObject(ContextExtensions)
        every { any<Context>().tryToOpenFile(any(), any()) } returns Unit
        val sharedFile = mockDownloadSharedFile.copy(state = SharedFileUi.State.Success(uri = ImmutableUri(Uri.EMPTY)))
        items = ImmutableList(listOf(sharedFile))
        composeTestRule.onNodeWithTag(FileShareItemTag).performClick()
        verify { any<Context>().tryToOpenFile(any(), any()) }
    }

    @Test
    fun userClicksSuccessStateAction_tryToOpenFileInvoked() {
        mockkObject(ContextExtensions)
        every { any<Context>().tryToOpenFile(any(), any()) } returns Unit
        val sharedFile = mockDownloadSharedFile.copy(state = SharedFileUi.State.Success(uri = ImmutableUri(Uri.EMPTY)))
        items = ImmutableList(listOf(sharedFile))
        val openFile = composeTestRule.activity.getString(R.string.kaleyra_fileshare_open_file)
        composeTestRule.onNodeWithContentDescription(openFile).performClick()
        verify { any<Context>().tryToOpenFile(any(), any()) }
    }

    @Test
    fun userClicksAvailableStateAction_onDownloadInvoked() {
        val sharedFile = mockDownloadSharedFile.copy(id = "fileId", state = SharedFileUi.State.Available)
        items = ImmutableList(listOf(sharedFile))
        val startDownload = composeTestRule.activity.getString(R.string.kaleyra_fileshare_download_descr)
        composeTestRule.onNodeWithContentDescription(startDownload).performClick()
        assertEquals("fileId", downloadId)
    }

    @Test
    fun userClicksPendingStateAction_onShareCancelInvoked() {
        val sharedFile = mockDownloadSharedFile.copy(id = "fileId", state = SharedFileUi.State.Pending)
        items = ImmutableList(listOf(sharedFile))
        val cancel = composeTestRule.activity.getString(R.string.kaleyra_fileshare_cancel)
        composeTestRule.onNodeWithContentDescription(cancel).performClick()
        assertEquals("fileId", cancelId)
    }

    @Test
    fun userClicksErrorStateActionOnUpload_onUploadInvoked() {
        val uriMock = mockk<Uri>()
        val sharedFile = mockUploadSharedFile.copy(uri = ImmutableUri(uriMock), state = SharedFileUi.State.Error)
        items = ImmutableList(listOf(sharedFile))
        val retry = composeTestRule.activity.getString(R.string.kaleyra_fileshare_retry)
        composeTestRule.onNodeWithContentDescription(retry).performClick()
        assertEquals(uriMock, uploadUri)
    }

    @Test
    fun userClicksErrorStateActionOnDownload_onDownloadInvoked() {
        val sharedFile = mockDownloadSharedFile.copy(id = "fileId", state = SharedFileUi.State.Error)
        items = ImmutableList(listOf(sharedFile))
        val retry = composeTestRule.activity.getString(R.string.kaleyra_fileshare_retry)
        composeTestRule.onNodeWithContentDescription(retry).performClick()
        assertEquals("fileId", downloadId)
    }

}