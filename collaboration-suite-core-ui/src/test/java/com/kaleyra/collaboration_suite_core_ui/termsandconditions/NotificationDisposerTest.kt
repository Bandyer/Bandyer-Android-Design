package com.kaleyra.collaboration_suite_core_ui.termsandconditions

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.kaleyra.collaboration_suite_core_ui.termsandconditions.notification.NotificationDisposer
import com.kaleyra.collaboration_suite_core_ui.termsandconditions.notification.NotificationDisposer.Companion.KEY_EXTRA_NOTIFICATION_ID
import com.kaleyra.collaboration_suite_core_ui.utils.PendingIntentExtensions
import com.kaleyra.collaboration_suite_core_ui.utils.TimeHelper
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
internal class NotificationDisposerTest {

    private val contextMock = mockk<Context>(relaxed = true)

    private val disposer = NotificationDisposer()

    @Before
    fun setUp() {
        mockkObject(TimeHelper)
        every { TimeHelper.getNow() } returns 1000L
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testDisposeAfter() {
        val notificationId = 10
        val alarmManagerMock = mockk<AlarmManager>(relaxed = true)
        every { contextMock.getSystemService(Context.ALARM_SERVICE) } returns alarmManagerMock

        NotificationDisposer.disposeAfter(contextMock, notificationId, time = 3000L)

        verify(exactly = 1) {
            alarmManagerMock.set(AlarmManager.RTC_WAKEUP, 4000L, withArg {
                assertEquals(notificationId, shadowOf(it).savedIntent.extras!!.getInt(KEY_EXTRA_NOTIFICATION_ID))
                assertEquals(NotificationDisposer::class.java, Class.forName(shadowOf(it).savedIntent.component!!.className))
                assertEquals(PendingIntentExtensions.oneShotFlags, shadowOf(it).flags)
            })
        }
    }

    @Test
    fun testRevokeDisposal() {
        val notificationId = 10
        val alarmManagerMock = mockk<AlarmManager>(relaxed = true)
        every { contextMock.getSystemService(Context.ALARM_SERVICE) } returns alarmManagerMock

        NotificationDisposer.revokeDisposal(contextMock, notificationId)

        verify(exactly = 1) {
            alarmManagerMock.cancel(withArg<PendingIntent> {
                assertEquals(notificationId, shadowOf(it).savedIntent.extras!!.getInt(KEY_EXTRA_NOTIFICATION_ID))
                assertEquals(NotificationDisposer::class.java, Class.forName(shadowOf(it).savedIntent.component!!.className))
                assertEquals(PendingIntentExtensions.oneShotFlags, shadowOf(it).flags)
            })
        }
    }

    @Test
    fun testCancelNotification() {
        val notificationId = 10
        val intentMock = mockk<Intent>(relaxed = true)
        val notificationManagerMock = mockk<NotificationManager>(relaxed = true)
        every { contextMock.getSystemService(Context.NOTIFICATION_SERVICE) } returns notificationManagerMock
        every { intentMock.getIntExtra(KEY_EXTRA_NOTIFICATION_ID, 0) } returns notificationId
        disposer.onReceive(contextMock, intentMock)
        verify(exactly = 1) { notificationManagerMock.cancel(notificationId) }
    }

}