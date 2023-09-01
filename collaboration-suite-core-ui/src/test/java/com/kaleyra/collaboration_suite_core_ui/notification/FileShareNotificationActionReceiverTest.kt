package com.kaleyra.collaboration_suite_core_ui.notification

import android.content.Context
import android.content.Intent
import com.kaleyra.collaboration_suite_core_ui.CallUI
import com.kaleyra.collaboration_suite_core_ui.KaleyraVideo
import com.kaleyra.collaboration_suite_core_ui.MainDispatcherRule
import com.kaleyra.collaboration_suite_core_ui.notification.fileshare.FileShareNotificationActionReceiver
import com.kaleyra.collaboration_suite_core_ui.notification.fileshare.FileShareNotificationActionReceiver.Companion.ACTION_DOWNLOAD
import com.kaleyra.collaboration_suite_core_ui.notification.fileshare.FileShareNotificationDelegate.Companion.EXTRA_DOWNLOAD_ID
import com.kaleyra.collaboration_suite_core_ui.onCallReady
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ContextExtensions
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ContextExtensions.goToLaunchingActivity
import io.mockk.coEvery
import io.mockk.every
import io.mockk.invoke
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
internal class FileShareNotificationActionReceiverTest {

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private val callMock = mockk<CallUI>(relaxed = true)

    private val contextMock = mockk<Context>()

    private val fileShareNotificationActionReceiver = spyk(FileShareNotificationActionReceiver(mainDispatcherRule.testDispatcher))

    @Before
    fun setUp() {
        mockkObject(ContextExtensions)
        mockkObject(NotificationManager)
        mockkStatic("com.kaleyra.collaboration_suite_core_ui.KaleyraVideoKt")
        every { KaleyraVideo.onCallReady(any(), captureLambda()) } answers { lambda<(CallUI) -> Unit>().invoke(callMock) }
        every { contextMock.goToLaunchingActivity() } returns Unit
        every { callMock.sharedFolder.download(any()) } returns mockk(relaxed = true)
        every { NotificationManager.cancel(any()) } returns Unit
        coEvery { fileShareNotificationActionReceiver.goAsync() } returns mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testOnReceiveWithCollaborationConfigured() = runTest {
        val downloadId = "downloadId"
        val intent = Intent(ACTION_DOWNLOAD).apply {
            putExtra(EXTRA_DOWNLOAD_ID, downloadId)
        }
        coEvery { fileShareNotificationActionReceiver.requestConfigure() } returns true
        fileShareNotificationActionReceiver.onReceive(mockk(relaxed = true), intent)
        advanceUntilIdle()
        verify { callMock.sharedFolder.download(downloadId) }
        verify { NotificationManager.cancel(downloadId.hashCode()) }
    }

    @Test
    fun testOnReceiveWithCollaborationNotConfigured() = runTest {
        coEvery { fileShareNotificationActionReceiver.requestConfigure() } returns false
        fileShareNotificationActionReceiver.onReceive(contextMock, mockk(relaxed = true))
        advanceUntilIdle()
        verify { contextMock.goToLaunchingActivity() }
    }
}