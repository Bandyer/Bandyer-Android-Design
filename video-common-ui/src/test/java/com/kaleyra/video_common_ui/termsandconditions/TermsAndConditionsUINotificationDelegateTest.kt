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

package com.kaleyra.video_common_ui.termsandconditions

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import com.kaleyra.video_common_ui.termsandconditions.broadcastreceiver.TermsAndConditionBroadcastReceiver
import com.kaleyra.video_common_ui.termsandconditions.notification.NotificationDisposer
import com.kaleyra.video_common_ui.termsandconditions.notification.TermsAndConditionsNotification
import com.kaleyra.video_common_ui.termsandconditions.notification.TermsAndConditionsUINotificationDelegate
import com.kaleyra.video_common_ui.termsandconditions.notification.TermsAndConditionsUINotificationDelegate.Companion.TERMS_AND_CONDITIONS_NOTIFICATION_ID
import io.mockk.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class TermsAndConditionsUINotificationDelegateTest {

    private val contextMock = mockk<Context>(relaxed = true)

    private val notificationManagerMock = mockk<NotificationManager>(relaxed = true)

    private val notificationMock = mockk<Notification>(relaxed = true)

    private val termsAndConditionsNotificationConfig = TermsAndConditionsUI.Config.Notification(
        title = "title",
        message = "message",
        enableFullscreen = false,
        timeout = null,
        dismissCallback = { }
    )

    private val delegate = TermsAndConditionsUINotificationDelegate(contextMock, termsAndConditionsNotificationConfig)

    @Before
    fun setUp() {
        mockkObject(NotificationDisposer)
        mockkConstructor(TermsAndConditionsNotification.Builder::class)
        every { contextMock.getSystemService(Context.NOTIFICATION_SERVICE) } returns notificationManagerMock
        every { contextMock.applicationContext.packageManager.getApplicationIcon(any<String>()) } returns mockk()
        every { contextMock.applicationContext.packageName } returns "packageName"
        every { NotificationDisposer.revokeDisposal(any(), any()) } returns Unit
        every { anyConstructed<TermsAndConditionsNotification.Builder>().build() } returns notificationMock
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testShowNotification() {
        val activityIntentMock = mockk<Intent>(relaxed = true)

        delegate.showNotification(activityIntentMock)

        verify(exactly = 1) { anyConstructed<TermsAndConditionsNotification.Builder>().title(termsAndConditionsNotificationConfig.title) }
        verify(exactly = 1) { anyConstructed<TermsAndConditionsNotification.Builder>().message(termsAndConditionsNotificationConfig.message) }
        verify(exactly = 1) { anyConstructed<TermsAndConditionsNotification.Builder>().contentIntent(withArg {
            assertEquals(activityIntentMock, shadowOf(it).savedIntent)
        }) }
        verify(exactly = 1) { anyConstructed<TermsAndConditionsNotification.Builder>().deleteIntent(withArg {
            assertEquals(TermsAndConditionBroadcastReceiver.ACTION_CANCEL, shadowOf(it).savedIntent.action)
            assertEquals("packageName", shadowOf(it).savedIntent.`package`)
        }) }
        verify(exactly = 0) { anyConstructed<TermsAndConditionsNotification.Builder>().fullscreenIntent(any()) }
        verify(exactly = 0) { anyConstructed<TermsAndConditionsNotification.Builder>().timeout(any()) }
        verify(exactly = 1) { notificationManagerMock.notify(TERMS_AND_CONDITIONS_NOTIFICATION_ID, notificationMock) }
    }

    @Test
    fun `add fullscreen intent if configuration's enableFullscreen is true`() {
        val activityIntentMock = mockk<Intent>(relaxed = true)
        val config = termsAndConditionsNotificationConfig.copy(enableFullscreen = true)
        val delegate = TermsAndConditionsUINotificationDelegate(contextMock, config)

        delegate.showNotification(activityIntentMock)

        verify(exactly = 1) { anyConstructed<TermsAndConditionsNotification.Builder>().fullscreenIntent(withArg {
            assertEquals(activityIntentMock, shadowOf(it).savedIntent)
        }) }
    }

    @Test
    fun `add timeout if configuration's timeout is set`() {
        val config = termsAndConditionsNotificationConfig.copy(timeout = 3000L)
        val delegate = TermsAndConditionsUINotificationDelegate(contextMock, config)

        delegate.showNotification(mockk())

        verify(exactly = 1) { anyConstructed<TermsAndConditionsNotification.Builder>().timeout(3000L) }
    }

    @Test
    fun testDismissNotification() {
        delegate.dismissNotification()

        verify(exactly = 1) { notificationManagerMock.cancel(TERMS_AND_CONDITIONS_NOTIFICATION_ID) }
        verify(exactly = 1) { NotificationDisposer.revokeDisposal(contextMock, TERMS_AND_CONDITIONS_NOTIFICATION_ID) }
    }


}