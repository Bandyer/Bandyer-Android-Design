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
import com.kaleyra.video_common_ui.R
import com.kaleyra.video_common_ui.notification.fileshare.FileShareNotification
import com.kaleyra.video_common_ui.notification.fileshare.FileShareNotificationActionReceiver
import com.kaleyra.video_common_ui.notification.fileshare.FileShareNotificationDelegate.Companion.EXTRA_DOWNLOAD_ID
import com.kaleyra.video_common_ui.notification.fileshare.FileShareNotificationManager
import com.kaleyra.video_common_ui.utils.PendingIntentExtensions
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows

@RunWith(RobolectricTestRunner::class)
class FileShareNotificationManagerTest {

    private val fileShareNotificationManager = object : FileShareNotificationManager {}

    @Before
    fun setUp() {
        mockkConstructor(FileShareNotification.Builder::class)
        every { anyConstructed<FileShareNotification.Builder>().contentTitle(any()) } answers { self as FileShareNotification.Builder }
        every { anyConstructed<FileShareNotification.Builder>().contentText(any()) } answers { self as FileShareNotification.Builder }
        every { anyConstructed<FileShareNotification.Builder>().contentIntent(any()) } answers { self as FileShareNotification.Builder }
        every { anyConstructed<FileShareNotification.Builder>().downloadIntent(any()) } answers { self as FileShareNotification.Builder }
        every { anyConstructed<FileShareNotification.Builder>().build() } returns mockk(relaxed = true)
    }

    @Test
    fun testBuildIncomingFileNotification() {
        val context = RuntimeEnvironment.getApplication()
        fileShareNotificationManager.buildIncomingFileNotification(
            context = context,
            username = "username",
            downloadId = "downloadId",
            activityClazz = this::class.java
        )
        verify(exactly = 1) {
            anyConstructed<FileShareNotification.Builder>().contentTitle(
                context.getString(
                    R.string.kaleyra_notification_user_sharing_file,
                    "username"
                )
            )
        }
        verify(exactly = 1) {
            anyConstructed<FileShareNotification.Builder>().contentText(
                context.getString(R.string.kaleyra_notification_download_file)
            )
        }
        verify(exactly = 1) {
            anyConstructed<FileShareNotification.Builder>().contentIntent(withArg {
                val intent = Shadows.shadowOf(it).savedIntent
                assertEquals(PendingIntentExtensions.updateFlags, Shadows.shadowOf(it).flags)
                assertEquals(Intent.ACTION_MAIN, intent.action)
                assert(intent.hasCategory(Intent.CATEGORY_LAUNCHER))
                assertEquals(Intent.FLAG_ACTIVITY_NEW_TASK, intent.flags)
                assertEquals(FileShareNotificationActionReceiver.ACTION_DOWNLOAD, intent.getStringExtra("notificationAction"))
            })
        }
        verify(exactly = 1) {
            anyConstructed<FileShareNotification.Builder>().downloadIntent(withArg {
                val intent = Shadows.shadowOf(it).savedIntent
                assertEquals(PendingIntentExtensions.updateFlags, Shadows.shadowOf(it).flags)
                assertEquals(Intent.ACTION_MAIN, intent.action)
                assert(intent.hasCategory(Intent.CATEGORY_LAUNCHER))
                assertEquals(Intent.FLAG_ACTIVITY_NEW_TASK, intent.flags)
                assertEquals(FileShareNotificationActionReceiver.ACTION_DOWNLOAD, intent.getStringExtra("notificationAction"))
                assertEquals("downloadId", intent.getStringExtra(EXTRA_DOWNLOAD_ID))
            })
        }
    }
}