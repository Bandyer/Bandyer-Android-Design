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
import android.content.Intent
import com.kaleyra.video_common_ui.termsandconditions.activity.TermsAndConditionsUIActivityDelegate
import com.kaleyra.video_common_ui.termsandconditions.constants.Constants
import com.kaleyra.video_common_ui.termsandconditions.model.TermsAndConditions
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TermsAndConditionsUIActivityDelegateTest {

    private val contextMock = mockk<Context>(relaxed = true)

    private var acceptCallbackInvoked = false

    private var declineCallbackInvoked = false

    private val termsAndConditionsActivityConfig = TermsAndConditionsUI.Config.Activity(
        title = "title",
        message = "message",
        acceptText = "acceptText",
        declineText = "declineText",
        acceptCallback = { acceptCallbackInvoked = true },
        declineCallback = { declineCallbackInvoked = true }
    )

    private val expectedTermsAndConditionsConfiguration = TermsAndConditions(
        termsAndConditionsActivityConfig.title,
        termsAndConditionsActivityConfig.message,
        termsAndConditionsActivityConfig.acceptText,
        termsAndConditionsActivityConfig.declineText
    )

    private val delegate = TermsAndConditionsUIActivityDelegate(contextMock, termsAndConditionsActivityConfig, this::class.java)

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testShowActivity() {
        delegate.showActivity()
        verify(exactly = 1) { contextMock.startActivity(withArg { assertActivityIntent(it) }) }
    }
    
    @Test
    fun testGetActivityIntent() {
        val activityIntent = delegate.getActivityIntent()
        assertActivityIntent(activityIntent)
    }

    private fun assertActivityIntent(intent: Intent) {
        val actualTermsAndConditionConfiguration = intent.extras!!.get(Constants.EXTRA_TERMS_AND_CONDITIONS_CONFIGURATION)
        assertEquals(Intent.FLAG_ACTIVITY_NEW_TASK, intent.flags)
        assertNotEquals(null, intent.extras)
        assertTrue(intent.extras!!.containsKey("enableTilt"))
        assertEquals(expectedTermsAndConditionsConfiguration, actualTermsAndConditionConfiguration)
    }
}