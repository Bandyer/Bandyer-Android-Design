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

package com.kaleyra.video_common_ui.notification

import android.content.Intent
import com.kaleyra.video_common_ui.notification.fileshare.FileShareVisibilityObserver
import com.kaleyra.video_common_ui.notification.fileshare.FileShareVisibilityObserver.Companion.ACTION_FILE_SHARE_DISPLAYED
import com.kaleyra.video_common_ui.notification.fileshare.FileShareVisibilityObserver.Companion.ACTION_FILE_SHARE_NOT_DISPLAYED
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FileShareVisibilityObserverTest {

    private val fileShareVisibilityObserver = FileShareVisibilityObserver()

    @Test
    fun testOnReceiveDisplayedAction() {
        fileShareVisibilityObserver.onReceive(mockk(), Intent(ACTION_FILE_SHARE_DISPLAYED))
        assertEquals(true, FileShareVisibilityObserver.isDisplayed.value)
    }

    @Test
    fun testOnReceiveNotDisplayedAction() {
        fileShareVisibilityObserver.onReceive(mockk(), Intent(ACTION_FILE_SHARE_NOT_DISPLAYED))
        assertEquals(false, FileShareVisibilityObserver.isDisplayed.value)
    }
}