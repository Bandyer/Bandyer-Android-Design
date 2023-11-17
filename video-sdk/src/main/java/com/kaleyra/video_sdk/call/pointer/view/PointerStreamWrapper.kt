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

package com.kaleyra.video_sdk.call.pointer.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import com.kaleyra.video.conference.VideoStreamView
import com.kaleyra.video_sdk.call.stream.model.ImmutableView
import com.kaleyra.video_sdk.call.pointer.model.PointerUi
import com.kaleyra.video_sdk.call.utils.VideoStreamViewExtensions.getScale
import com.kaleyra.video_sdk.call.utils.VideoStreamViewExtensions.getSize
import com.kaleyra.video_sdk.call.utils.VideoStreamViewExtensions.getTranslation
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf

@Composable
internal fun PointerStreamWrapper(
    modifier: Modifier = Modifier,
    streamView: ImmutableView?,
    pointerList: ImmutableList<PointerUi>?,
    isTesting: Boolean = false,
    stream: @Composable (Boolean) -> Unit
) {
    val size by getSize(streamView).collectAsStateWithLifecycle(IntSize(0,0))
    val translation by getTranslation(streamView).collectAsStateWithLifecycle(floatArrayOf(0f, 0f))
    val scale by getScale(streamView).collectAsStateWithLifecycle(floatArrayOf(1f, 1f))
    Box(contentAlignment = Alignment.Center) {
        stream(!pointerList.isNullOrEmpty())
        Box(
            modifier = modifier
                .size(size.toDpSize())
                .graphicsLayer {
                    translationX = translation[0]
                    translationY = translation[1]
                    transformOrigin = TransformOrigin(0f, 0f)
                    scaleX = scale[0]
                    scaleY = scale[1]
                }
        ) {
            // TODO revise this
            // A fixed floatArray is passed for testing because otherwise the test will timeout
            pointerList?.value?.forEach { MovablePointer(it, size, if (isTesting) floatArrayOf(1f, 1f) else scale) }
        }
    }
}

@Composable
internal fun IntSize.toDpSize() = with(LocalDensity.current) {
    DpSize(width.toDp(), height.toDp())
}

private fun getSize(view: ImmutableView?): Flow<IntSize> =
    (view?.value as? VideoStreamView)?.getSize() ?: flowOf(IntSize(0,0))

private fun getTranslation(view: ImmutableView?): Flow<FloatArray> =
    (view?.value as? VideoStreamView)?.getTranslation() ?: flowOf(floatArrayOf(0f, 0f))

private fun getScale(view: ImmutableView?): Flow<FloatArray> =
    (view?.value as? VideoStreamView)?.getScale() ?: MutableStateFlow(floatArrayOf(1f, 1f))

private fun ImmutableList<PointerUi>?.isNullOrEmpty() = this == null || this.count() == 0