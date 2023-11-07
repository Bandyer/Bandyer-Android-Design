package com.kaleyra.video_sdk.call.utils

import com.kaleyra.video.conference.StreamView
import com.kaleyra.video.conference.VideoStreamView
import com.kaleyra.video_sdk.call.stream.model.ImmutableView

object StreamViewSettings {

    fun ImmutableView.thumbnailSettings(scaleType: StreamView.ScaleType = StreamView.ScaleType.Fill(1f)): ImmutableView {
        val view = value
        if (view is VideoStreamView) {
            view.zoomGesturesEnabled.value = false
            if (view.scaleType.value != scaleType) {
                view.scaleType.value = scaleType
            }
        }
        return this
    }

    fun ImmutableView.featuredSettings(scaleType: StreamView.ScaleType = StreamView.ScaleType.Fill()): ImmutableView {
        val view = value
        if (view is VideoStreamView) {
            view.zoomGesturesEnabled.value = true
            if (view.scaleType.value != scaleType) {
                view.scaleType.value = scaleType
            }
        }
        return this
    }

    fun ImmutableView.pipSettings(): ImmutableView {
        val view = value
        if (view is VideoStreamView) {
            view.zoomGesturesEnabled.value = false
            view.scaleType.value = StreamView.ScaleType.Fit
        }
        return this
    }
}