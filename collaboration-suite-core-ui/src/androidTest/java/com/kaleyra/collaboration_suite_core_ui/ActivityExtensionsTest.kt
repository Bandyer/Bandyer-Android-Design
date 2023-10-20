package com.kaleyra.collaboration_suite_core_ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.platform.app.InstrumentationRegistry
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ActivityExtensions.moveToFront
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ContextExtensions
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ContextExtensions.doesFileExists
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ContextExtensions.tryToOpenFile
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.UriExtensions
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.UriExtensions.getMimeType
import io.mockk.every
import io.mockk.mockkObject
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