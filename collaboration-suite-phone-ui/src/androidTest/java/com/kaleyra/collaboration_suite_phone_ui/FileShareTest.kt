package com.kaleyra.collaboration_suite_phone_ui

import android.net.Uri
import android.text.format.Formatter
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_core_ui.utils.TimestampUtils
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.FileShare
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.FileShareItemTag
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.ProgressIndicatorTag
import com.kaleyra.collaboration_suite_phone_ui.call.compose.model.Transfer
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FileShareTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val download = Transfer.Download(
        "download.txt",
        Transfer.FileType.Miscellaneous,
        40000L,
        "Keanu",
        .4f,
        3254234L,
        Uri.EMPTY,
        Transfer.State.InProgress,
        {},
        {}
    )

    private val upload = Transfer.Upload(
        "upload.txt",
        Transfer.FileType.Media,
        23333L,
        "Mario",
        .7f,
        324234L,
        Uri.EMPTY,
        Transfer.State.InProgress,
        {},
        {}
    )

    private var items by mutableStateOf(ImmutableList(listOf<Transfer>()))

    private var fabClicked = false

    private var closeClicked = false

    @Before
    fun setUp() {
        composeTestRule.setContent {
            FileShare(
                items = items,
                onFabClick = { fabClicked = true },
                onCloseClick = { closeClicked = true }
            )
        }
    }

    @Test
    fun emptyItems_noItemsUIDisplayed() {
        val title = composeTestRule.activity.getString(R.string.kaleyra_no_file_shared)
        val subtitle = composeTestRule.activity.getString(R.string.kaleyra_click_to_share_file)
        items = ImmutableList(listOf(upload))
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
        items = ImmutableList(listOf(download))
        val iconDescription = composeTestRule.activity.getString(R.string.kaleyra_fileshare_add_description)
        val text = composeTestRule.activity.getString(R.string.kaleyra_fileshare_add).uppercase()
        composeTestRule.onNodeWithContentDescription(iconDescription).assertIsDisplayed()
        composeTestRule.onNodeWithText(text).assertDoesNotExist()
    }

    @Test
    fun userClicksFab_onFabClickInvoked() {
        val add = composeTestRule.activity.getString(R.string.kaleyra_fileshare_add_description)
        composeTestRule.onNodeWithContentDescription(add).performClick()
        assert(fabClicked)
    }

    @Test
    fun userClicksClose_onCloseClickInvoked() {
        val close = composeTestRule.activity.getString(R.string.kaleyra_close)
        composeTestRule.onNodeWithContentDescription(close).performClick()
        assert(closeClicked)
    }

    @Test
    fun itemStateAvailable_itemIsNotEnabled() {
        items = ImmutableList(listOf(download.copy(state = Transfer.State.Available)))
        composeTestRule.onNodeWithTag(FileShareItemTag).assertIsNotEnabled()
    }

    @Test
    fun itemStatePending_itemIsNotEnabled() {
        items = ImmutableList(listOf(download.copy(state = Transfer.State.Pending)))
        composeTestRule.onNodeWithTag(FileShareItemTag).assertIsNotEnabled()
    }

    @Test
    fun itemStateInProgress_itemIsNotEnabled() {
        items = ImmutableList(listOf(download.copy(state = Transfer.State.InProgress)))
        composeTestRule.onNodeWithTag(FileShareItemTag).assertIsNotEnabled()
    }

    @Test
    fun itemStateSuccess_itemIsEnabled() {
        items = ImmutableList(listOf(download.copy(state = Transfer.State.Success)))
        composeTestRule.onNodeWithTag(FileShareItemTag).assertIsEnabled()
    }

    @Test
    fun itemStateError_itemIsNotEnabled() {
        items = ImmutableList(listOf(download.copy(state = Transfer.State.Error)))
        composeTestRule.onNodeWithTag(FileShareItemTag).assertIsNotEnabled()
    }

    @Test
    fun itemStateCancelled_itemIsEnabled() {
        items = ImmutableList(listOf(download.copy(state = Transfer.State.Success)))
        composeTestRule.onNodeWithTag(FileShareItemTag).assertIsEnabled()
    }

    @Test
    fun itemStateSuccess_usersClicksItem_onClickInvoked() {
        var clicked = false
        items = ImmutableList(listOf(download.copy(state = Transfer.State.Success, onClick = { clicked = true })))
        composeTestRule.onNodeWithTag(FileShareItemTag).performClick()
        assert(clicked)
    }

    @Test
    fun usersClicksItemAction_onActionClickInvoked() {
        var clicked = false
        val cancel = composeTestRule.activity.getString(R.string.kaleyra_fileshare_cancel)
        items = ImmutableList(listOf(download.copy(state = Transfer.State.InProgress, onActionClick = { clicked = true })))
        composeTestRule.onNodeWithContentDescription(cancel).performClick()
        assert(clicked)
    }

    @Test
    fun uploadItemError_uploadErrorTextDisplayed() {
        items = ImmutableList(listOf(upload.copy(state = Transfer.State.Error)))
        val error = composeTestRule.activity.getString(R.string.kaleyra_fileshare_upload_error)
        composeTestRule.onNodeWithText(error).assertIsDisplayed()
    }

    @Test
    fun downloadItemError_downloadErrorTextDisplayed() {
        items = ImmutableList(listOf(download.copy(state = Transfer.State.Error)))
        val error = composeTestRule.activity.getString(R.string.kaleyra_fileshare_download_error)
        composeTestRule.onNodeWithText(error).assertIsDisplayed()
    }

    @Test
    fun downloadItem_usernameDisplayed() {
        items = ImmutableList(listOf(download.copy(sender = "username")))
        composeTestRule.onNodeWithText("username").assertIsDisplayed()
    }

    @Test
    fun uploadItem_youDisplayed() {
        items = ImmutableList(listOf(upload.copy(sender = "username")))
        val you = composeTestRule.activity.getString(R.string.kaleyra_fileshare_you)
        composeTestRule.onNodeWithText(you).assertIsDisplayed()
    }

    @Test
    fun transferItem_fileNameDisplayed() {
        items = ImmutableList(listOf(upload.copy(fileName = "fileName")))
        composeTestRule.onNodeWithText("fileName").assertIsDisplayed()
    }

    @Test
    fun mediaFile_mediaFileIconDisplayed() {
        items = ImmutableList(listOf(upload.copy(fileType = Transfer.FileType.Media)))
        val media = composeTestRule.activity.getString(R.string.kaleyra_fileshare_media)
        composeTestRule.onNodeWithContentDescription(media).assertIsDisplayed()
    }

    @Test
    fun miscellaneousFile_miscellaneousFileIconDisplayed() {
        items = ImmutableList(listOf(upload.copy(fileType = Transfer.FileType.Miscellaneous)))
        val miscellaneous = composeTestRule.activity.getString(R.string.kaleyra_fileshare_miscellaneous)
        composeTestRule.onNodeWithContentDescription(miscellaneous).assertIsDisplayed()
    }

    @Test
    fun archiveFile_archiveFileIconDisplayed() {
        items = ImmutableList(listOf(upload.copy(fileType = Transfer.FileType.Archive)))
        val archive = composeTestRule.activity.getString(R.string.kaleyra_fileshare_archive)
        composeTestRule.onNodeWithContentDescription(archive).assertIsDisplayed()
    }

    @Test
    fun uploadItem_fileSizeFormattedDisplayed() {
        items = ImmutableList(listOf(upload.copy(fileSize = 30000)))
        val text = Formatter.formatShortFileSize(composeTestRule.activity, 30000)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun downloadItem_successState_fileSizeFormattedDisplayed() {
        items = ImmutableList(listOf(download.copy(fileSize = 30000, state = Transfer.State.Success)))
        val text = Formatter.formatShortFileSize(composeTestRule.activity, 30000)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun downloadItem_inProgressState_fileSizeFormattedDisplayed() {
        items = ImmutableList(listOf(download.copy(fileSize = 30000, state = Transfer.State.InProgress)))
        val text = Formatter.formatShortFileSize(composeTestRule.activity, 30000)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun downloadItem_availableState_NASizeDisplayed() {
        items = ImmutableList(listOf(download.copy(fileSize = 30000, state = Transfer.State.Available)))
        val na = composeTestRule.activity.getString(R.string.kaleyra_fileshare_na)
        composeTestRule.onNodeWithText(na).assertIsDisplayed()
    }

    @Test
    fun downloadItem_pendingState_NASizeDisplayed() {
        items = ImmutableList(listOf(download.copy(fileSize = 30000, state = Transfer.State.Pending)))
        val na = composeTestRule.activity.getString(R.string.kaleyra_fileshare_na)
        composeTestRule.onNodeWithText(na).assertIsDisplayed()
    }

    @Test
    fun downloadItem_errorState_NASizeDisplayed() {
        items = ImmutableList(listOf(download.copy(fileSize = 30000, state = Transfer.State.Error)))
        val na = composeTestRule.activity.getString(R.string.kaleyra_fileshare_na)
        composeTestRule.onNodeWithText(na).assertIsDisplayed()
    }

    @Test
    fun downloadItem_cancelledState_NASizeDisplayed() {
        items = ImmutableList(listOf(download.copy(fileSize = 30000, state = Transfer.State.Cancelled)))
        val na = composeTestRule.activity.getString(R.string.kaleyra_fileshare_na)
        composeTestRule.onNodeWithText(na).assertIsDisplayed()
    }

    @Test
    fun transferProgress_progressBarValueUpdated() {
        items = ImmutableList(listOf(download.copy(progress = .74f)))
        composeTestRule
            .onNodeWithTag(ProgressIndicatorTag)
            .assertIsDisplayed()
            .assertRangeInfoEquals(ProgressBarRangeInfo(current =.74f, range = 0f..1f))
    }

    @Test
    fun itemStateSuccess_formattedTimestampDisplayed() {
        items = ImmutableList(listOf(download.copy(state = Transfer.State.Success, time = 45555)))
        val text = TimestampUtils.parseTime(45555)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun itemStateAvailable_formattedTimestampDisplayed() {
        items = ImmutableList(listOf(download.copy(state = Transfer.State.Available, time = 45555)))
        val text = TimestampUtils.parseTime(45555)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun itemStatePending_progressTextDisplayed() {
        items = ImmutableList(listOf(download.copy(state = Transfer.State.Pending, progress = .74f)))
        val percentage = composeTestRule.activity.getString(R.string.kaleyra_fileshare_progress, 74)
        composeTestRule.onNodeWithText(percentage).assertIsDisplayed()
    }

    @Test
    fun itemStateInProgress_progressTextDisplayed() {
        items = ImmutableList(listOf(download.copy(state = Transfer.State.InProgress, progress = .74f)))
        val percentage = composeTestRule.activity.getString(R.string.kaleyra_fileshare_progress, 74)
        composeTestRule.onNodeWithText(percentage).assertIsDisplayed()
    }

    @Test
    fun itemStateError_progressTextDisplayed() {
        items = ImmutableList(listOf(download.copy(state = Transfer.State.Error, progress = .74f)))
        val percentage = composeTestRule.activity.getString(R.string.kaleyra_fileshare_progress, 74)
        composeTestRule.onNodeWithText(percentage).assertIsDisplayed()
    }

    @Test
    fun itemStateCancelled_progressTextDisplayed() {
        items = ImmutableList(listOf(download.copy(state = Transfer.State.Cancelled, progress = .74f)))
        val percentage = composeTestRule.activity.getString(R.string.kaleyra_fileshare_progress, 74)
        composeTestRule.onNodeWithText(percentage).assertIsDisplayed()
    }

    @Test
    fun uploadItem_uploadIconDisplayed() {
        items = ImmutableList(listOf(upload))
        val upload = composeTestRule.activity.getString(R.string.kaleyra_fileshare_upload)
        composeTestRule.onNodeWithContentDescription(upload).assertIsDisplayed()
    }

    @Test
    fun downloadItem_downloadIconDisplayed() {
        items = ImmutableList(listOf(download))
        val download = composeTestRule.activity.getString(R.string.kaleyra_fileshare_download)
        composeTestRule.onNodeWithContentDescription(download).assertIsDisplayed()
    }
}