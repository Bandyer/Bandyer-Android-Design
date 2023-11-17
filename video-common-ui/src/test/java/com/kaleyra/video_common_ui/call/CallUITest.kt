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

package com.kaleyra.video_common_ui.call

import android.content.Context
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.KaleyraUIProvider
import com.kaleyra.video_common_ui.MainDispatcherRule
import com.kaleyra.video_common_ui.utils.AppLifecycle
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions.isActivityRunning
import com.kaleyra.video_utils.ContextRetainer
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CallUITest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val context = mockk<Context>(relaxed = true)

    @Before
    fun setUp() {
        mockkObject(ContextExtensions)
        mockkObject(KaleyraUIProvider)
        mockkObject(ContextRetainer)
        mockkObject(AppLifecycle)
        every { ContextRetainer.context } returns context
        every { KaleyraUIProvider.startCallActivity(any()) } answers { }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun appInForeground_callShowSuccessful() {
        val callUI = CallUI(call = mockk(), activityClazz = this::class.java)
        every { AppLifecycle.isInForeground } returns MutableStateFlow(true)
        val success = callUI.show()
        assertEquals(true, success)
        verify(exactly = 1) { KaleyraUIProvider.startCallActivity(callUI.activityClazz) }
    }

    @Test
    fun appInBackground_callShowFailed() {
        val callUI = CallUI(call = mockk(), activityClazz = this::class.java)
        every { AppLifecycle.isInForeground } returns MutableStateFlow(false)
        val success = callUI.show()
        assertEquals(false, success)
        verify(exactly = 0) { KaleyraUIProvider.startCallActivity(callUI.activityClazz) }
    }

    @Test
    fun activityRunning_setDisplayModeSuccessful() {
        val callUI = CallUI(call = mockk(), activityClazz = this::class.java)
        every { context.isActivityRunning(callUI.activityClazz) } returns true
        val success = callUI.setDisplayMode(CallUI.DisplayMode.PictureInPicture)
        val actualDisplayMode = callUI.displayModeEvent.replayCache.firstOrNull()?.displayMode
        assertEquals(true, success)
        assertEquals(CallUI.DisplayMode.PictureInPicture, actualDisplayMode)
    }

    @Test
    fun activityDestroyed_setDisplayModeFailed() {
        val callUI = CallUI(call = mockk(), activityClazz = this::class.java)
        every { context.isActivityRunning(callUI.activityClazz) } returns false
        val success = callUI.setDisplayMode(CallUI.DisplayMode.PictureInPicture)
        val actualDisplayMode = callUI.displayModeEvent.replayCache.firstOrNull()?.displayMode
        assertEquals(false, success)
        assertEquals(null, actualDisplayMode)
    }

}