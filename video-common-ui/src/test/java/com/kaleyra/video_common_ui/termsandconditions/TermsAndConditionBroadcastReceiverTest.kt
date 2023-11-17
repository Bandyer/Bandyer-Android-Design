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
import com.kaleyra.video_common_ui.termsandconditions.broadcastreceiver.TermsAndConditionBroadcastReceiver
import com.kaleyra.video_common_ui.termsandconditions.broadcastreceiver.TermsAndConditionBroadcastReceiver.Companion.ACTION_ACCEPT
import com.kaleyra.video_common_ui.termsandconditions.broadcastreceiver.TermsAndConditionBroadcastReceiver.Companion.ACTION_CANCEL
import com.kaleyra.video_common_ui.termsandconditions.broadcastreceiver.TermsAndConditionBroadcastReceiver.Companion.ACTION_DECLINE
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TermsAndConditionBroadcastReceiverTest {

    private val contextMock = mockk<Context>(relaxed = true)

    private val receiver = spyk(
        object : TermsAndConditionBroadcastReceiver() {
            override fun onActionAccept() = Unit
            override fun onActionDecline() = Unit
            override fun onActionCancel() = Unit
        }
    )

    @Test
    fun testOnReceiveAcceptAction() {
        receiver.onReceive(contextMock, Intent(ACTION_ACCEPT))
        verify(exactly = 1) { receiver.onActionAccept() }
        verify(exactly = 1) { contextMock.unregisterReceiver(receiver) }
    }

    @Test
    fun testOnReceiveDeclineAction() {
        receiver.onReceive(contextMock, Intent(ACTION_DECLINE))
        verify(exactly = 1) { receiver.onActionDecline() }
        verify(exactly = 1) { contextMock.unregisterReceiver(receiver) }
    }

    @Test
    fun testOnReceiveCancelAction() {
        receiver.onReceive(contextMock, Intent(ACTION_CANCEL))
        verify(exactly = 1) { receiver.onActionCancel() }
        verify(exactly = 1) { contextMock.unregisterReceiver(receiver) }
    }

    @Test
    fun testOnReceiveGenericAction() {
        receiver.onReceive(contextMock, Intent("genericAction"))
        verify(exactly = 0) { contextMock.unregisterReceiver(receiver) }
    }
}