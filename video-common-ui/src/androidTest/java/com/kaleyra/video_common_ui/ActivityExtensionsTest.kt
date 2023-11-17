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

package com.kaleyra.video_common_ui

import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import com.kaleyra.video_common_ui.utils.extensions.ActivityExtensions.moveToFront
import org.junit.Assert.assertEquals
import org.junit.Test

class ActivityExtensionsTest {

    @Test
    fun testMoveToFront() {
        Intents.init()
        val scenario = ActivityScenario
            .launch(DummyActivity::class.java)
            .moveToState(Lifecycle.State.STARTED)
            .onActivity { it.moveToFront() }
        Intents.intended(IntentMatchers.hasFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT))
        assertEquals(true, scenario.state.isAtLeast(Lifecycle.State.RESUMED))
        Intents.release()
        scenario.close()
    }
}