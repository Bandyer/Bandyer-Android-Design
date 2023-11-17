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

import android.content.Context
import com.kaleyra.video_common_ui.termsandconditions.activity.TermsAndConditionsUIActivityDelegate
import com.kaleyra.video_common_ui.termsandconditions.notification.TermsAndConditionsUINotificationDelegate
import com.kaleyra.video_common_ui.utils.AppLifecycle
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TermsAndConditionsUITest {

    private lateinit var termsAndConditionsUI: TermsAndConditionsUI

    private val contextMock = mockk<Context>(relaxed = true)

    private val notificationConfig = spyk(TermsAndConditionsUI.Config.Notification("", "", { }, false))

    private val activityConfig = spyk(TermsAndConditionsUI.Config.Activity("", "", "", "", { }, { }))

    @Before
    fun setUp() {
        mockkObject(AppLifecycle)
        mockkConstructor(TermsAndConditionsUIActivityDelegate::class)
        mockkConstructor(TermsAndConditionsUINotificationDelegate::class)
        every { anyConstructed<TermsAndConditionsUIActivityDelegate>().showActivity()  } returns Unit
        every { anyConstructed<TermsAndConditionsUINotificationDelegate>().showNotification(any())  } returns Unit
        every { anyConstructed<TermsAndConditionsUINotificationDelegate>().dismissNotification()  } returns Unit
        termsAndConditionsUI = spyk(TermsAndConditionsUI(contextMock, this::class.java, notificationConfig, activityConfig))
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `show terms activity if app is in foreground`() {
        every { AppLifecycle.isInForeground } returns MutableStateFlow(true)
        termsAndConditionsUI.show(contextMock)
        verify { anyConstructed<TermsAndConditionsUIActivityDelegate>().showActivity() }
    }

    @Test
    fun `show terms notification if app is in background`() {
        every { AppLifecycle.isInForeground } returns MutableStateFlow(false)
        termsAndConditionsUI.show(contextMock)
        verify { anyConstructed<TermsAndConditionsUINotificationDelegate>().showNotification(any()) }
    }

    @Test
    fun `register for broadcast receiver actions`() {
        termsAndConditionsUI.show(contextMock)
        verify(exactly = 1) { termsAndConditionsUI.registerForTermAndConditionAction(any()) }
    }

    @Test
    fun testDismiss() {
        termsAndConditionsUI.dismiss()
        verify { anyConstructed<TermsAndConditionsUINotificationDelegate>().dismissNotification() }
    }

    @Test
    fun testOnActionAccept() {
        every { activityConfig.acceptCallback() } returns Unit
        termsAndConditionsUI.onActionAccept()
        verify(exactly = 1) { activityConfig.acceptCallback() }
    }

    @Test
    fun testOnActionDecline() {
        every { activityConfig.declineCallback() } returns Unit
        termsAndConditionsUI.onActionDecline()
        verify(exactly = 1) { activityConfig.declineCallback() }
    }

    @Test
    fun testOnActionCancel() {
        every { notificationConfig.dismissCallback() } returns Unit
        termsAndConditionsUI.onActionCancel()
        verify(exactly = 1) { notificationConfig.dismissCallback() }
    }
}