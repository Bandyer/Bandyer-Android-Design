package com.kaleyra.collaboration_suite_core_ui.termsandconditions

import android.content.Context
import android.content.Intent
import com.kaleyra.collaboration_suite_core_ui.termsandconditions.broadcastreceiver.TermsAndConditionBroadcastReceiver
import com.kaleyra.collaboration_suite_core_ui.termsandconditions.broadcastreceiver.TermsAndConditionBroadcastReceiver.Companion.ACTION_ACCEPT
import com.kaleyra.collaboration_suite_core_ui.termsandconditions.broadcastreceiver.TermsAndConditionBroadcastReceiver.Companion.ACTION_CANCEL
import com.kaleyra.collaboration_suite_core_ui.termsandconditions.broadcastreceiver.TermsAndConditionBroadcastReceiver.Companion.ACTION_DECLINE
import com.kaleyra.collaboration_suite_core_ui.termsandconditions.notification.TermsAndConditionsUINotificationDelegate
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.Assert
import org.junit.Assert.assertEquals
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