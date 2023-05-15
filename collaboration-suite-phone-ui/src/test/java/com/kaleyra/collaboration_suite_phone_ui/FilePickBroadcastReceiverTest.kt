package com.kaleyra.collaboration_suite_phone_ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.filepick.FilePickBroadcastReceiver
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class FilePickBroadcastReceiverTest {

    private val contextMock = mockk<Context>(relaxed = true)

    private val receiver = FilePickBroadcastReceiver()

    @Test
    fun testOnReceive() = runTest {
        val uriMock = mockk<Uri>()
        receiver.onReceive(contextMock, Intent(FilePickBroadcastReceiver.ACTION_FILE_PICK_EVENT).apply {
          putExtra("uri", uriMock)
        })
        assertEquals(uriMock, FilePickBroadcastReceiver.fileUri.first())
    }
}