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

import android.graphics.Matrix
import androidx.compose.ui.unit.IntSize
import com.kaleyra.video.conference.StreamView
import com.kaleyra.video.conference.VideoStreamView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

internal object VideoStreamViewExtensions {

    fun VideoStreamView.getSize(): Flow<IntSize> = videoSize.map {
        IntSize(it.width, it.height)
    }

    fun VideoStreamView.getTranslation(): Flow<FloatArray> =
        this.getTransformationMatrix().map { matrix ->
            if (!matrix.isAffine) floatArrayOf(0f, 0f)
            else {
                val values = FloatArray(9)
                matrix.getValues(values)
                val tx = values[Matrix.MTRANS_X]
                val ty = values[Matrix.MTRANS_Y]
                floatArrayOf(tx, ty)
            }
        }

    fun VideoStreamView.getScale(): Flow<FloatArray> =
        this.getTransformationMatrix().map { matrix ->
            if (!matrix.isAffine) floatArrayOf(0f, 0f)
            else {
                val values = FloatArray(9)
                matrix.getValues(values)
                val tx = values[Matrix.MSCALE_X]
                val ty = values[Matrix.MSCALE_Y]
                floatArrayOf(tx, ty)
            }
        }

    private fun VideoStreamView.getTransformationMatrix(): Flow<Matrix> {
        return this.state
            .filterIsInstance<StreamView.State.Rendering>()
            .flatMapLatest { it.matrix }
    }
}