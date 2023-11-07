package com.kaleyra.video_common_ui

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions.doesFileExists
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions.isActivityRunning
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions.tryToOpenFile
import com.kaleyra.video_common_ui.utils.extensions.UriExtensions
import com.kaleyra.video_common_ui.utils.extensions.UriExtensions.getMimeType
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ContextExtensionsTest {

    @Test
    fun fileExists_tryToOpenFileSucceed_fileIsOpened() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val uri = context.resourceUri(com.kaleyra.video_common_ui.test.R.drawable.kaleyra_logo)
        mockkObject(ContextExtensions)
        every { any<Context>().doesFileExists(any()) } returns true
        mockkObject(UriExtensions)
        every { any<Uri>().getMimeType(any()) } returns "image/png"
        Intents.init()
        context.tryToOpenFile(uri) {}
        intended(hasData(uri))
        intended(hasType("image/png"))
        intended(hasFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP))
        intended(hasFlag(Intent.FLAG_GRANT_READ_URI_PERMISSION))
        Intents.release()
    }

    @Test
    fun fileExists_tryToOpenFileFail_onFailureInvoked() {
        var doesFileExists = false
        val context = InstrumentationRegistry.getInstrumentation().context
        mockkObject(ContextExtensions)
        every { any<Context>().doesFileExists(any()) } returns true
        context.tryToOpenFile(mockk()) { doesFileExists = it }
        assertEquals(true, doesFileExists)
    }

    @Test
    fun fileNotExists_tryToOpenFileFail_onFailureInvoked() {
        var doesFileExists = true
        val context = InstrumentationRegistry.getInstrumentation().context
        mockkObject(ContextExtensions)
        every { any<Context>().doesFileExists(any()) } returns false
        context.tryToOpenFile(mockk()) { doesFileExists = it }
        assertEquals(false, doesFileExists)
    }

    private fun Context.resourceUri(resourceId: Int): Uri = with(resources) {
        Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(getResourcePackageName(resourceId))
            .appendPath(getResourceTypeName(resourceId))
            .appendPath(getResourceEntryName(resourceId))
            .build()
    }

    @Test
    fun activityDestroyed_isActivityRunning_false() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val scenario = ActivityScenario.launch(DummyActivity::class.java)
        scenario.close()
        assertEquals(false, context.isActivityRunning(DummyActivity::class.java))
    }

    @Test
    fun activityLaunched_isActivityRunning_true() {
        val context = InstrumentationRegistry.getInstrumentation().context
        ActivityScenario.launch(DummyActivity::class.java)
        assertEquals(true, context.isActivityRunning(DummyActivity::class.java))
    }
}