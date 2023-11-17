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