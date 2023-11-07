package com.kaleyra.video_sdk

import com.kaleyra.video.conference.StreamView
import com.kaleyra.video.conference.VideoStreamView
import com.kaleyra.video_sdk.call.stream.model.ImmutableView
import com.kaleyra.video_sdk.call.utils.StreamViewSettings.featuredSettings
import com.kaleyra.video_sdk.call.utils.StreamViewSettings.pipSettings
import com.kaleyra.video_sdk.call.utils.StreamViewSettings.thumbnailSettings
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class StreamViewSettingsTest {

    @Test
    fun testDefaultFeaturedSettings() {
        val view = ImmutableView(VideoStreamView(RuntimeEnvironment.getApplication()))
        view.featuredSettings()
        val videoStreamView = view.value as VideoStreamView
        assertEquals(StreamView.ScaleType.Fill(), videoStreamView.scaleType.value)
    }
    
    @Test
    fun testSetCustomFeaturedSettings() {
        val view = ImmutableView(VideoStreamView(RuntimeEnvironment.getApplication()))
        view.featuredSettings(StreamView.ScaleType.Fit)
        val videoStreamView = view.value as VideoStreamView
        assertEquals(StreamView.ScaleType.Fit, videoStreamView.scaleType.value)
    }

    @Test
    fun testDefaultThumbnailSettings() {
        val view = ImmutableView(VideoStreamView(RuntimeEnvironment.getApplication()))
        view.thumbnailSettings()
        val videoStreamView = view.value as VideoStreamView
        assertEquals(StreamView.ScaleType.Fill(1f), videoStreamView.scaleType.value)
    }

    @Test
    fun testSetCustomThumbnailSettings() {
        val view = ImmutableView(VideoStreamView(RuntimeEnvironment.getApplication()))
        view.thumbnailSettings(StreamView.ScaleType.Fit)
        val videoStreamView = view.value as VideoStreamView
        assertEquals(StreamView.ScaleType.Fit, videoStreamView.scaleType.value)
    }

    @Test
    fun testPipSettings() {
        val view = ImmutableView(VideoStreamView(RuntimeEnvironment.getApplication()))
        view.pipSettings()
        val videoStreamView = view.value as VideoStreamView
        assertEquals(StreamView.ScaleType.Fit, videoStreamView.scaleType.value)
    }
}