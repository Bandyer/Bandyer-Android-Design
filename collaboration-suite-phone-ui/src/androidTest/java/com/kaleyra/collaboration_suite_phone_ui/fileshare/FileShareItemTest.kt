package com.kaleyra.collaboration_suite_phone_ui.fileshare

import android.net.Uri
import android.text.format.Formatter
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.collaboration_suite_core_ui.utils.TimestampUtils
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.compose.ImmutableUri
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.ProgressIndicatorTag
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.model.FileUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.model.SharedFileUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.model.mockDownloaSharedFile
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.model.mockUploadSharedFile
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.view.FileShareItem
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FileShareItemTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var sharedFile by mutableStateOf(mockUploadSharedFile)

    private var isActionClicked = false

    @Before
    fun setUp() {
        composeTestRule.setContent {
            FileShareItem(
                sharedFile = sharedFile,
                onActionClick = { isActionClicked = true }
            )
        }
    }

    @After
    fun tearDown() {
        sharedFile = mockUploadSharedFile
        isActionClicked = false
    }

    @Test
    fun usersClicksAction_onActionClickInvoked() {
        val download = composeTestRule.activity.getString(R.string.kaleyra_fileshare_download_descr)
        sharedFile = mockDownloaSharedFile.copy(state = SharedFileUi.State.Available)
        composeTestRule.onNodeWithContentDescription(download).performClick()
        assert(isActionClicked)
    }

    @Test
    fun uploadErrorState_uploadErrorTextDisplayed() {
        sharedFile = mockUploadSharedFile.copy(state = SharedFileUi.State.Error)
        val error = composeTestRule.activity.getString(R.string.kaleyra_fileshare_upload_error)
        composeTestRule.onNodeWithText(error).assertIsDisplayed()
    }

    @Test
    fun downloadErrorState_downloadErrorTextDisplayed() {
        sharedFile = mockDownloaSharedFile.copy(state = SharedFileUi.State.Error)
        val error = composeTestRule.activity.getString(R.string.kaleyra_fileshare_download_error)
        composeTestRule.onNodeWithText(error).assertIsDisplayed()
    }

    @Test
    fun download_usernameDisplayed() {
        sharedFile = mockDownloaSharedFile.copy(sender = "username")
        composeTestRule.onNodeWithText("username").assertIsDisplayed()
    }

    @Test
    fun upload_youDisplayed() {
        sharedFile = mockUploadSharedFile.copy(sender = "username")
        val you = composeTestRule.activity.getString(R.string.kaleyra_fileshare_you)
        composeTestRule.onNodeWithText(you).assertIsDisplayed()
    }

    @Test
    fun fileNameDisplayed() {
        val file = mockUploadSharedFile.file.copy(name = "fileName")
        sharedFile = mockUploadSharedFile.copy(file = file)
        Espresso.onView(withText("fileName")).check(matches(isDisplayed()))
    }

    @Test
    fun mediaFileType_mediaFileIconDisplayed() {
        val file = mockUploadSharedFile.file.copy(type = FileUi.Type.Media)
        sharedFile = mockUploadSharedFile.copy(file = file)
        val media = composeTestRule.activity.getString(R.string.kaleyra_fileshare_media)
        composeTestRule.onNodeWithContentDescription(media).assertIsDisplayed()
    }

    @Test
    fun miscellaneousFileType_miscellaneousFileIconDisplayed() {
        val file = mockUploadSharedFile.file.copy(type = FileUi.Type.Miscellaneous)
        sharedFile = mockUploadSharedFile.copy(file = file)
        val miscellaneous = composeTestRule.activity.getString(R.string.kaleyra_fileshare_miscellaneous)
        composeTestRule.onNodeWithContentDescription(miscellaneous).assertIsDisplayed()
    }

    @Test
    fun archiveFileType_archiveFileIconDisplayed() {
        val file = mockUploadSharedFile.file.copy(type = FileUi.Type.Archive)
        sharedFile = mockUploadSharedFile.copy(file = file)
        val archive = composeTestRule.activity.getString(R.string.kaleyra_fileshare_archive)
        composeTestRule.onNodeWithContentDescription(archive).assertIsDisplayed()
    }

    @Test
    fun notNullFileSize_fileSizeFormattedDisplayed() {
        val file = mockUploadSharedFile.file.copy(size = 30000)
        sharedFile = mockUploadSharedFile.copy(file = file)
        val text = Formatter.formatShortFileSize(composeTestRule.activity, 30000)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun nullFileSize_inProgressState_fileSizeFormattedDisplayed() {
        val file = mockUploadSharedFile.file.copy(size = null)
        sharedFile = mockDownloaSharedFile.copy(file = file)
        val na = composeTestRule.activity.getString(R.string.kaleyra_fileshare_na)
        composeTestRule.onNodeWithText(na).assertIsDisplayed()
    }

    @Test
    fun inProgressState_progressBarValueUpdated() {
        sharedFile = mockDownloaSharedFile.copy(state = SharedFileUi.State.InProgress(progress = .74f))
        composeTestRule
            .onNodeWithTag(ProgressIndicatorTag)
            .assertIsDisplayed()
            .assertRangeInfoEquals(ProgressBarRangeInfo(current =.74f, range = 0f..1f))
    }

    @Test
    fun successState_formattedTimestampDisplayed() {
        sharedFile = mockDownloaSharedFile.copy(state = SharedFileUi.State.Success(ImmutableUri(Uri.EMPTY)), time = 45555)
        val text = TimestampUtils.parseTime(45555)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun availableState_formattedTimestampDisplayed() {
        sharedFile = mockDownloaSharedFile.copy(state = SharedFileUi.State.Available, time = 45555)
        val text = TimestampUtils.parseTime(45555)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun pendingState_formattedTimestampDisplayed() {
        sharedFile = mockDownloaSharedFile.copy(state = SharedFileUi.State.Pending, time = 45555)
        val text = TimestampUtils.parseTime(45555)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun inProgressState_progressTextDisplayed() {
        sharedFile = mockDownloaSharedFile.copy(state = SharedFileUi.State.InProgress(progress = .74f))
        val percentage = composeTestRule.activity.getString(R.string.kaleyra_fileshare_progress, 74)
        composeTestRule.onNodeWithText(percentage).assertIsDisplayed()
    }

    @Test
    fun errorState_formattedTimestampDisplayed() {
        sharedFile = mockDownloaSharedFile.copy(state = SharedFileUi.State.Error, time = 45555)
        val text = TimestampUtils.parseTime(45555)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun cancelledState_formattedTimestampDisplayed() {
        sharedFile = mockDownloaSharedFile.copy(state = SharedFileUi.State.Cancelled, time = 45555)
        val text = TimestampUtils.parseTime(45555)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun upload_uploadIconDisplayed() {
        sharedFile = mockUploadSharedFile
        val upload = composeTestRule.activity.getString(R.string.kaleyra_fileshare_upload)
        composeTestRule.onNodeWithContentDescription(upload).assertIsDisplayed()
    }

    @Test
    fun download_downloadIconDisplayed() {
        sharedFile = mockDownloaSharedFile
        val download = composeTestRule.activity.getString(R.string.kaleyra_fileshare_download)
        composeTestRule.onNodeWithContentDescription(download).assertIsDisplayed()
    }
}