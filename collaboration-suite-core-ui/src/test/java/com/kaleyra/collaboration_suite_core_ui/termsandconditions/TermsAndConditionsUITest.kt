package com.kaleyra.collaboration_suite_core_ui.termsandconditions

import android.content.Context
import com.kaleyra.collaboration_suite_core_ui.termsandconditions.activity.TermsAndConditionsUIActivityDelegate
import com.kaleyra.collaboration_suite_core_ui.termsandconditions.notification.TermsAndConditionsUINotificationDelegate
import com.kaleyra.collaboration_suite_core_ui.utils.AppLifecycle
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
        termsAndConditionsUI.show()
        verify { anyConstructed<TermsAndConditionsUIActivityDelegate>().showActivity() }
    }

    @Test
    fun `show terms notification if app is in background`() {
        every { AppLifecycle.isInForeground } returns MutableStateFlow(false)
        termsAndConditionsUI.show()
        verify { anyConstructed<TermsAndConditionsUINotificationDelegate>().showNotification(any()) }
    }

    @Test
    fun `register for broadcast receiver actions`() {
        termsAndConditionsUI.show()
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