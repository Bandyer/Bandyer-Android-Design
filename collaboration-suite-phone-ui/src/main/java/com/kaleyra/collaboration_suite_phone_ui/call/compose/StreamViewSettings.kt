package com.kaleyra.collaboration_suite_phone_ui.call.compose

import com.kaleyra.collaboration_suite.phonebox.StreamView
import com.kaleyra.collaboration_suite.phonebox.VideoStreamView

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