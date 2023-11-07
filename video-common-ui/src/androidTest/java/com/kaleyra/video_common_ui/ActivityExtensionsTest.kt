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