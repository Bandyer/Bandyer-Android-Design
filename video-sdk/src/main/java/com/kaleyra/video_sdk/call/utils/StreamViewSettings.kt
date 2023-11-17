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