/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.video_sdk.ui.call.fileshare

import android.net.Uri
import android.text.format.Formatter
import android.webkit.MimeTypeMap
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.kaleyra.video_common_ui.utils.TimestampUtils
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import com.kaleyra.video_sdk.call.fileshare.ProgressIndicatorTag
import com.kaleyra.video_sdk.call.fileshare.model.SharedFileUi
import com.kaleyra.video_sdk.call.fileshare.model.mockDownloadSharedFile
import com.kaleyra.video_sdk.call.fileshare.model.mockUploadSharedFile
import com.kaleyra.video_sdk.call.fileshare.view.FileShareItem
import io.mockk.mockk
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
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
        sharedFile = mockDownloadSharedFile.copy(state = SharedFileUi.State.Available)
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
        sharedFile = mockDownloadSharedFile.copy(state = SharedFileUi.State.Error)
        val error = composeTestRule.activity.getString(R.string.kaleyra_fileshare_download_error)
        composeTestRule.onNodeWithText(error).assertIsDisplayed()
    }

    @Test
    fun download_usernameDisplayed() {
        sharedFile = mockDownloadSharedFile.copy(sender = "username")
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
        sharedFile = mockUploadSharedFile.copy(name = "fileName")
        // Check  the content description since this is a view
        composeTestRule.onNodeWithContentDescription("fileName").assertIsDisplayed()
    }

    @Test
    fun mediaFileType_mediaFileIconDisplayed() {
        shadowOf(MimeTypeMap.getSingleton()).addExtensionMimeTypMapping("jpeg", "image/jpeg")
        val uri = Uri.parse("file.jpeg")
        sharedFile = mockUploadSharedFile.copy(uri = ImmutableUri(uri))
        val media = composeTestRule.activity.getString(R.string.kaleyra_fileshare_media)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription(media).assertIsDisplayed()
    }

    @Test
    fun miscellaneousFileType_miscellaneousFileIconDisplayed() {
        val uri = Uri.parse("file.r")
        sharedFile = mockUploadSharedFile.copy(uri = ImmutableUri(uri))
        val miscellaneous = composeTestRule.activity.getString(R.string.kaleyra_fileshare_miscellaneous)
        composeTestRule.onNodeWithContentDescription(miscellaneous).assertIsDisplayed()
    }

    @Test
    fun archiveFileType_archiveFileIconDisplayed() {
        shadowOf(MimeTypeMap.getSingleton()).addExtensionMimeTypMapping("zip", "application/zip")
        val uri = Uri.parse("file.zip")
        sharedFile = mockUploadSharedFile.copy(uri = ImmutableUri(uri))
        val archive = composeTestRule.activity.getString(R.string.kaleyra_fileshare_archive)
        composeTestRule.onNodeWithContentDescription(archive).assertIsDisplayed()
    }

    @Test
    fun notNullFileSize_fileSizeFormattedDisplayed() {
        sharedFile = mockUploadSharedFile.copy(size = 30000)
        val text = Formatter.formatShortFileSize(composeTestRule.activity, 30000)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun nullFileSize_inProgressState_fileSizeFormattedDisplayed() {
        sharedFile = mockDownloadSharedFile.copy(size = null)
        val na = composeTestRule.activity.getString(R.string.kaleyra_fileshare_na)
        composeTestRule.onNodeWithText(na).assertIsDisplayed()
    }

    @Test
    fun inProgressState_progressBarValueUpdated() {
        sharedFile = mockDownloadSharedFile.copy(state = SharedFileUi.State.InProgress(progress = .74f))
        composeTestRule
            .onNodeWithTag(ProgressIndicatorTag)
            .assertIsDisplayed()
            .assertRangeInfoEquals(ProgressBarRangeInfo(current =.74f, range = 0f..1f))
    }

    @Test
    fun successState_progressBarIsFull() {
        sharedFile = mockDownloadSharedFile.copy(state = SharedFileUi.State.Success(mockk()))
        composeTestRule
            .onNodeWithTag(ProgressIndicatorTag)
            .assertIsDisplayed()
            .assertRangeInfoEquals(ProgressBarRangeInfo(current = 1f, range = 0f..1f))
    }

    @Test
    fun successState_formattedTimestampDisplayed() {
        sharedFile = mockDownloadSharedFile.copy(state = SharedFileUi.State.Success(ImmutableUri(Uri.EMPTY)), time = 45555)
        val text = TimestampUtils.parseTime(45555)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun availableState_formattedTimestampDisplayed() {
        sharedFile = mockDownloadSharedFile.copy(state = SharedFileUi.State.Available, time = 45555)
        val text = TimestampUtils.parseTime(45555)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun pendingState_formattedTimestampDisplayed() {
        sharedFile = mockDownloadSharedFile.copy(state = SharedFileUi.State.Pending, time = 45555)
        val text = TimestampUtils.parseTime(45555)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun inProgressState_progressTextDisplayed() {
        sharedFile = mockDownloadSharedFile.copy(state = SharedFileUi.State.InProgress(progress = .74f))
        val percentage = composeTestRule.activity.getString(R.string.kaleyra_fileshare_progress, 74)
        composeTestRule.onNodeWithText(percentage).assertIsDisplayed()
    }

    @Test
    fun errorState_formattedTimestampDisplayed() {
        sharedFile = mockDownloadSharedFile.copy(state = SharedFileUi.State.Error, time = 45555)
        val text = TimestampUtils.parseTime(45555)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun cancelledState_formattedTimestampDisplayed() {
        sharedFile = mockDownloadSharedFile.copy(state = SharedFileUi.State.Cancelled, time = 45555)
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
        sharedFile = mockDownloadSharedFile
        val download = composeTestRule.activity.getString(R.string.kaleyra_fileshare_download)
        composeTestRule.onNodeWithContentDescription(download).assertIsDisplayed()
    }
}