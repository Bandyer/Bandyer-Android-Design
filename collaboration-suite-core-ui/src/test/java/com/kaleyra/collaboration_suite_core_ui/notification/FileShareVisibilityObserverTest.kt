package com.kaleyra.collaboration_suite_core_ui.notification

import android.content.Intent
import com.kaleyra.collaboration_suite_core_ui.notification.fileshare.FileShareVisibilityObserver
import com.kaleyra.collaboration_suite_core_ui.notification.fileshare.FileShareVisibilityObserver.Companion.ACTION_FILE_SHARE_DISPLAYED
import com.kaleyra.collaboration_suite_core_ui.notification.fileshare.FileShareVisibilityObserver.Companion.ACTION_FILE_SHARE_NOT_DISPLAYED
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FileShareVisibilityObserverTest {

    private val fileShareVisibilityObserver = FileShareVisibilityObserver()

    @Test
    fun testOnReceiveDisplayedAction() {
        fileShareVisibilityObserver.onReceive(mockk(), Intent(ACTION_FILE_SHARE_DISPLAYED))
        assertEquals(true, FileShareVisibilityObserver.isDisplayed.value)
    }

    @Test
    fun testOnReceiveNotDisplayedAction() {
        fileShareVisibilityObserver.onReceive(mockk(), Intent(ACTION_FILE_SHARE_NOT_DISPLAYED))
        assertEquals(false, FileShareVisibilityObserver.isDisplayed.value)
    }
}