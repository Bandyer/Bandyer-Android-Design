package com.kaleyra.collaboration_suite_core_ui.termsandconditions

import android.content.Context
import android.content.Intent
import com.kaleyra.collaboration_suite_core_ui.termsandconditions.activity.TermsAndConditionsUIActivityDelegate
import com.kaleyra.collaboration_suite_core_ui.termsandconditions.activity.TermsAndConditionsUIActivityDelegate.Companion.ACTION_ACCEPT
import com.kaleyra.collaboration_suite_core_ui.termsandconditions.activity.TermsAndConditionsUIActivityDelegate.Companion.ACTION_DECLINE
import com.kaleyra.collaboration_suite_core_ui.termsandconditions.constants.Constants
import com.kaleyra.collaboration_suite_core_ui.termsandconditions.model.TermsAndConditions
import io.mockk.mockk
import io.mockk.verify
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

    @Test
    fun testShowActivity() {
        delegate.showActivity()
        verify(exactly = 1) {
            contextMock.registerReceiver(delegate, withArg {
                assertTrue(it.hasAction(ACTION_ACCEPT))
                assertTrue(it.hasAction(ACTION_DECLINE))
            })
        }
        verify(exactly = 1) {
            contextMock.startActivity(withArg { intent ->
                checkActivityIntent(intent)
            })
        }
    }

    @Test
    fun testOnReceiveAcceptAction() {
        delegate.onReceive(contextMock, Intent(ACTION_ACCEPT))
        assertTrue(acceptCallbackInvoked)
        verify(exactly = 1) { contextMock.unregisterReceiver(delegate) }
    }

    @Test
    fun testOnReceiveDeclineAction() {
        delegate.onReceive(contextMock, Intent(ACTION_DECLINE))
        assertTrue(declineCallbackInvoked)
        verify(exactly = 1) { contextMock.unregisterReceiver(delegate) }
    }

    @Test
    fun testOnReceiveGenericAction() {
        delegate.onReceive(contextMock, Intent("genericAction"))
        verify(exactly = 0) { contextMock.unregisterReceiver(delegate) }
    }

    @Test
    fun testGetActivityIntent() {
        val activityIntent = delegate.getActivityIntent()
        checkActivityIntent(activityIntent)
    }

    private fun checkActivityIntent(intent: Intent) {
        val actualTermsAndConditionConfiguration = intent.extras!!.get(Constants.EXTRA_TERMS_AND_CONDITIONS_CONFIGURATION)
        assertEquals(Intent.FLAG_ACTIVITY_NEW_TASK, intent.flags)
        assertNotEquals(null, intent.extras)
        assertTrue(intent.extras!!.containsKey("enableTilt"))
        assertEquals(expectedTermsAndConditionsConfiguration, actualTermsAndConditionConfiguration)
    }
}