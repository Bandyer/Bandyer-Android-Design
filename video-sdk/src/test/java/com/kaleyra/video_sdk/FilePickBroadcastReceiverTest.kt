package com.kaleyra.video_sdk

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.kaleyra.video_sdk.call.fileshare.filepick.FilePickBroadcastReceiver
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeoutOrNull
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
        var result: Uri? = null
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            result = FilePickBroadcastReceiver.fileUri.first()
        }
        receiver.onReceive(contextMock, Intent(FilePickBroadcastReceiver.ACTION_FILE_PICK_EVENT).apply {
          putExtra("uri", uriMock)
        })
        assertEquals(uriMock, result)
    }

    @Test
    fun `test file uri flow does not have a replay value`() = runTest {
        val uriMock = mockk<Uri>()
        receiver.onReceive(contextMock, Intent(FilePickBroadcastReceiver.ACTION_FILE_PICK_EVENT).apply {
            putExtra("uri", uriMock)
        })
        val result = withTimeoutOrNull(100) {
            FilePickBroadcastReceiver.fileUri.first()
        }
        assertEquals(null, result)
    }

}